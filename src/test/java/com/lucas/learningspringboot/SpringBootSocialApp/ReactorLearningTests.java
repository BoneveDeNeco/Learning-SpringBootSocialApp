package com.lucas.learningspringboot.SpringBootSocialApp;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class ReactorLearningTests {

	@Test
	public void fluxTest() {
		Flux<String> flux = Flux.just("yellow", "green", "blue", "white", "red", "purple", "orange");
		
		Flux<String> upper = flux
				.log()
				.map(String::toUpperCase);
		
		upper.subscribe(System.out::println);
		
		upper.subscribe(new LimitedSubscriber());
		
		//Handling in background thread
		upper.subscribeOn(Schedulers.parallel())
			.subscribe(new LimitedSubscriber());
		
		//One in each thread
		flux.log()
			.flatMap(value -> 
				Mono.just(value.toUpperCase())
					.subscribeOn(Schedulers.parallel()),
				2)
			.subscribe(value -> {
				System.out.println("Consumed: " + value);
			});
		
		upper.subscribeOn(Schedulers.newParallel("Sub"))
			.publishOn(Schedulers.newParallel("Pub"), 2)
			.subscribe(value -> {
				System.out.println("Consumed_1: " + value);
			});
			
	}

	private static class LimitedSubscriber implements Subscriber<String> {

		private long count = 0;
		private Subscription subscription;

		@Override
		public void onSubscribe(Subscription s) {
			this.subscription = s;
			subscription.request(2);
		}

		@Override
		public void onNext(String t) {
			count++;
			if (count >= 2) {
				count = 0;
				subscription.request(2);
			}
		}

		@Override
		public void onError(Throwable t) {
		}

		@Override
		public void onComplete() {

		}
	};
}
