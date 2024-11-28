package dev.lucaargolo.charta.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class TransparentLinkedList<E> extends LinkedList<E> {

    public TransparentLinkedList() {
        super();
    }
    
    private final List<Consumer<TransparentLinkedList<E>>> consumers = new LinkedList<>();

    public void addConsumer(Consumer<TransparentLinkedList<E>> listener) {
        consumers.add(listener);
    }
    
    public void removeConsumer(Consumer<TransparentLinkedList<E>> listener) {
        consumers.remove(listener);
    }
    
    public void removeAllConsumers() {
        consumers.clear();
    }
    
    private void change() {
        consumers.forEach(consumer -> consumer.accept(this));
    }

    @Override
    public boolean add(E e) {
        boolean result = super.add(e);
        this.change();
        return result;
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        this.change();
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        boolean result = super.addAll(index, c);
        this.change();
        return result;
    }

    @Override
    public void addFirst(E e) {
        super.addFirst(e);
        this.change();
    }

    @Override
    public void addLast(E e) {
        super.addLast(e);
        this.change();
    }

    @Override
    public boolean offer(E e) {
        boolean result = super.offer(e);
        this.change();
        return result;
    }

    @Override
    public boolean offerFirst(E e) {
        boolean result = super.offerFirst(e);
        this.change();
        return result;
    }

    @Override
    public boolean offerLast(E e) {
        boolean result = super.offerLast(e);
        this.change();
        return result;
    }

    @Override
    public void push(E e) {
        super.push(e);
        this.change();
    }

    @Override
    public E pop() {
        E result = super.pop();
        this.change();
        return result;
    }
    
    @Override
    public E poll() {
        E result = super.poll();
        this.change();
        return result;
    }

    @Override
    public E pollFirst() {
        E result = super.pollFirst();
        this.change();
        return result;
    }

    @Override
    public E pollLast() {
        E result = super.pollLast();
        this.change();
        return result;
    }

    @Override
    public boolean remove(Object o) {
        boolean result = super.remove(o);
        this.change();
        return result;
    }

    @Override
    public E remove() {
        E result = super.remove();
        this.change();
        return result;
    }

    @Override
    public E remove(int index) {
        E result = super.remove(index);
        this.change();
        return result;
    }

    @Override
    public E removeFirst() {
        E result = super.removeFirst();
        this.change();
        return result;
    }

    @Override
    public E removeLast() {
        E result = super.removeLast();
        this.change();
        return result;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
        this.change();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        boolean result = super.removeAll(c);
        this.change();
        return result;
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        boolean result = super.removeIf(filter);
        this.change();
        return result;
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        boolean result = super.removeFirstOccurrence(o);
        this.change();
        return result;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        boolean result = super.removeLastOccurrence(o);
        this.change();
        return result;
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        super.replaceAll(operator);
        this.change();
    }

    @Override
    public void clear() {
        super.clear();
        this.change();
    }

    
}
