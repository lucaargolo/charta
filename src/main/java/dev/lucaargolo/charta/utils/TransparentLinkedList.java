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
        this.change();
        return super.add(e);
    }

    @Override
    public void add(int index, E element) {
        this.change();
        super.add(index, element);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        this.change();
        return super.addAll(index, c);
    }

    @Override
    public void addFirst(E e) {
        this.change();
        super.addFirst(e);
    }

    @Override
    public void addLast(E e) {
        this.change();
        super.addLast(e);
    }

    @Override
    public boolean offer(E e) {
        this.change();
        return super.offer(e);
    }

    @Override
    public boolean offerFirst(E e) {
        this.change();
        return super.offerFirst(e);
    }

    @Override
    public boolean offerLast(E e) {
        this.change();
        return super.offerLast(e);
    }

    @Override
    public void push(E e) {
        this.change();
        super.push(e);
    }

    @Override
    public E pop() {
        this.change();
        return super.pop();
    }
    
    @Override
    public E poll() {
        this.change();
        return super.poll();
    }

    @Override
    public E pollFirst() {
        this.change();
        return super.pollFirst();
    }

    @Override
    public E pollLast() {
        this.change();
        return super.pollLast();
    }

    @Override
    public boolean remove(Object o) {
        this.change();
        return super.remove(o);
    }

    @Override
    public E remove() {
        this.change();
        return super.remove();
    }

    @Override
    public E remove(int index) {
        this.change();
        return super.remove(index);
    }

    @Override
    public E removeFirst() {
        this.change();
        return super.removeFirst();
    }

    @Override
    public E removeLast() {
        this.change();
        return super.removeLast();
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        this.change();
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        this.change();
        return super.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super E> filter) {
        this.change();
        return super.removeIf(filter);
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        this.change();
        return super.removeFirstOccurrence(o);
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        this.change();
        return super.removeLastOccurrence(o);
    }

    @Override
    public void replaceAll(UnaryOperator<E> operator) {
        this.change();
        super.replaceAll(operator);
    }

    @Override
    public void clear() {
        this.change();
        super.clear();
    }

    
}
