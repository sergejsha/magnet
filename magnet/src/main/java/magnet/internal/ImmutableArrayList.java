package magnet.internal;

import java.util.AbstractList;

/** Subject to change. For internal use only. */
public class ImmutableArrayList<E> extends AbstractList<E> {

    private final E[] elements;

    public ImmutableArrayList(E[] elements) {
        this.elements = elements;
    }

    @Override public E get(int i) {
        if (i < 0 || i >= elements.length) {
            throw new IndexOutOfBoundsException(
                    String.format(
                            "Cannot find element with index %s, array length: %s", i, elements.length));
        }
        return elements[i];
    }

    @Override public int size() {
        return elements.length;
    }
}
