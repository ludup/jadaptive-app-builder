package com.jadaptive.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompoundIterable<T> implements Iterable<T> {

	static Logger log = LoggerFactory.getLogger(CompoundIterable.class);
	
	Collection<Iterable<T>> iterators = new ArrayList<>();
	
	public CompoundIterable() {	
	}
	
	@SafeVarargs
	public CompoundIterable(Iterable<T>... it) {
		iterators.addAll(Arrays.asList(it));
	}
	
	public CompoundIterable(Collection<Iterable<T>> it) {
		iterators.addAll(it);
	}
	
	
	public void add(Iterable<T> it) {
		iterators.add(it);
	}
	
	@Override
	public Iterator<T> iterator() {
		return new CompoundIterator();
	}

	class CompoundIterator implements Iterator<T> {

		Iterator<T> current = null;
		List<Iterable<T>> iterators;
		
		CompoundIterator() {
			iterators = new ArrayList<>(CompoundIterable.this.iterators);
		}
		
		@Override
		public boolean hasNext() {
			ensureReady();
			if(Objects.isNull(current)) {
				return false;
			}
			boolean next = current.hasNext();
			log.info("hasNext {}", next);
			return next;
		}

		@Override
		public T next() {
			log.info("Loading next");
			ensureReady();
			if(Objects.isNull(current)) {
				throw new IllegalStateException();
			}
			if(!current.hasNext()) {
				throw new IllegalStateException();
			}
			return current.next();
		}
		
		private void ensureReady() {
			while((Objects.isNull(current) || !current.hasNext()) && !iterators.isEmpty()) {
				log.info("Changing iterators");
				current = iterators.remove(0).iterator();
			}
		}
	}
}
