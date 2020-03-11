package com.jadaptive.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class CompoundIterable<T> implements Iterable<T> {

	Collection<Iterable<T>> iterators = new ArrayList<>();
	
	public CompoundIterable() {	
	}
	
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
			return current.hasNext();
		}

		@Override
		public T next() {
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
			if((Objects.isNull(current) || !current.hasNext()) && !iterators.isEmpty()) {
				current = iterators.remove(0).iterator();
			}
		}
	}
}
