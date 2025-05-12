package dev.lucaargolo.charta.utils;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;

public class ReversedListView {

    public static <T> List<T> reversed(List<T> list) {
        Objects.requireNonNull(list);
        return new AbstractList<>() {
            @Override
            public T get(int index) {
                return list.get(size() - 1 - index);
            }

            @Override
            public T set(int index, T element) {
                return list.set(size() - 1 - index, element);
            }

            @Override
            public int size() {
                return list.size();
            }

            @Override
            public void add(int index, T element) {
                list.add(size() - index, element);
            }

            @Override
            public T remove(int index) {
                return list.remove(size() - 1 - index);
            }
        };
    }
}
