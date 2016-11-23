import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A linked list implementation of the list ADT. The list is doubly linked. The data of each list element
 * is hosted in {@link Element} which also provides linking capabilities for the list.
 *
 * @since 2016.11.11
 *
 * @author Weinan Qiu
 */
public class LinkedList {

    // We keep track of the head of the linked list,
    // so we can traverse from the start.
    private Element head;

    // We keep track of the tail of the linked list,
    // so we can traverse from the end.
    private Element tail;

    // We keep track of the size of the list so we can
    // reduce size calculation from an operation of O(N)
    // down to O(1)
    private int size;

    /**
     * Default constructor
     */
    public LinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Add a new piece of data at the specified position. This method
     * throws {@link RuntimeException} when the required position is
     * out of the bound of the list size or the provided data is null.
     *
     * @param data the data to be added to the list, must not be null.
     * @param pos the position to insert the provided data, must be
     *            from 0 (insert as head) to size of list (insert as tail)
     */
    public void add(Object data, int pos) {

        // Check if position is out of bound
        if (pos < 0 || pos > this.size)
            throw new RuntimeException("index out of bounds");
            // Check if data is null
        else if (data == null)
            throw new RuntimeException("no data provided");

        Element elem = new Element(data);

        // Handle the special case where the new element will be inserted as
        // both head and tail
        if (this.size == 0) {
            this.head = elem;
            this.tail = elem;
        }
        // Handle the insertion of the common case
        // Note that much of the specific link breaking/adding is handled
        // by Element itself.
        else {
            // Attach new element as predecessor of the current head if required position is 0
            if (pos == 0)
                this.head.setPrevious(elem);
                // (Or) attach new element as the successor of the required position minus one
            else {
                Element p = this.getElem(pos - 1);
                if (p != null) {
                    elem.setNext(p.getNext());
                    p.setNext(elem);
                }
            }

            // Refresh the head and tail if necessary
            if (!elem.hasPrevious())
                this.head = elem;
            if (!elem.hasNext())
                this.tail = elem;
        }

        // increment list size
        this.size++;
    }

    /**
     * Remove a data from the linked list at the request position. Throws
     * {@link RuntimeException} when the asked position is out of bounds.
     *
     * @param pos the position to remove, must be from 0 to size (exclusive).
     */
    public void remove(int pos) {
        // perform range check
        if (pos < 0 || pos > this.size - 1)
            throw new RuntimeException("index out of bounds");

        // Special case of removing head, we delegate new head to the
        // successor of the current head if there is one.
        if (pos == 0) {
            this.head = this.head.getNext();
            if (this.head != null)
                this.head.setPrevious(null);
        }
        // Special case of removing tail, we delegate new tail to the
        // predecessor of the current tail if there is one.
        else if (pos == this.size - 1) {
            this.tail = this.tail.getPrevious();
            if (this.tail != null)
                this.tail.setNext(null);
        }
        // Common case of removing something in between head and tail
        else {
            Element c = this.getElem(pos);
            c.getPrevious().setNext(c.getNext());
        }

        // decrement list size
        this.size--;
    }

    /**
     * Retrieve the element at the specified position. Returns null if the asked
     * position is out of bounds. This method is used as an internal helper for
     * {@link #get(int)}. It also optimizes the traversing complexity by distinguishing
     * the value of the asked index. If the index belongs to the first half of the list,
     * it traverses from head, else it traverses from tail.
     *
     * @param pos request element position, should be between 0 and size (exclusive)
     *
     * @return the element at the requested position.
     */
    private Element getElem(int pos) {
        // range check and short circuit the return
        if (pos < 0 || pos > this.size - 1)
            return null;

        Element cur = null;
        // Traverse from the head to find the value if the index is among first half
        if (pos < size / 2) {
            cur = this.head;
            for (int i = 0; i < pos; i++)
                cur = cur.getNext();
        }
        // Traverse from the tail to find the value if the index is among second half
        else {
            cur = this.tail;
            for (int i = this.size - 1; i > pos; i--)
                cur = cur.getPrevious();
        }

        return cur;
    }

    /**
     * Get the data value at the specified index. Returns null if the asked position is out
     * of bounds.
     *
     * @param pos requested element position, should be between 0 and size (exclusive)
     *
     * @return the data value at the requested position.
     */
    public Object get(int pos) {
        Element elem = this.getElem(pos);
        return elem == null ? null : elem.getData();
    }

    /**
     * Get the size of the list.
     *
     * @return size of the list.
     */
    public int getSize() {
        return size;
    }

    /**
     * Find a data value in the list according to some criteria. This function utilizes functional programming
     * paradigm to avoid the complexity of accessing an element via index inside a for loop, which in the worse
     * case is O(N^2) performance. Here we traverse the list once, hence the worse case performance is O(N).
     *
     * @param predicate Callback to determine whether the currently traversed element is desired as return value.
     *                  Return true if the value meets desire, false otherwise
     *
     * @return the element in the list where the predicate callback returns true
     */
    public Object find(Predicate<Object> predicate) {
        for (Element cur = this.head; cur != null; cur = cur.getNext()) {
            if (predicate.test(cur.getData())) {
                return cur.getData();
            }
        }
        return null;
    }

    /**
     * Perform some action on each list data value in sequence. This function utilizes functional programming
     * paradigm to avoid bad time complexity similar to {@link #find(Predicate)}.
     *
     * @param consumer operation logic performed on each traversed data value.
     */
    public void forEach(Consumer<Object> consumer) {
        for (Element cur = this.head; cur != null; cur = cur.getNext()) {
            consumer.accept(cur.getData());
        }
    }

    /**
     * Perform some action on each list data value in reversed sequence. This function is similar to
     * {@link #forEach(Consumer)} except that it traverses the list from its tail.
     *
     * @param consumer operation logic performed on each traversed data value.
     */
    public void forEachReversed(Consumer<Object> consumer) {
        for (Element cur = this.tail; cur != null; cur = cur.getPrevious()) {
            consumer.accept(cur.getData());
        }
    }

    /**
     * Element in the linked list. Serving as a wrapper for data value and provides
     * linking capabilities to other elements in order to form a list.
     *
     * @since 2016.11.11
     *
     * @author Weinan Qiu
     */
    private static class Element {

        // Data of an element, cannot be changed once set.
        private final Object data;

        // Previous element
        private Element previous;

        // Next element
        private Element next;

        public Element(Object data) {
            this.data = data;
        }

        public Object getData() {
            return data;
        }

        public Element getPrevious() {
            return previous;
        }

        /**
         * Set the previous element link. The provided element is allowed to be null, which
         * effectively appointing this element to be the head.
         *
         * @param previous the element to be the predecessor of this element, or NULL.
         */
        public void setPrevious(Element previous) {
            // break the current predecessor's next link to this
            if (this.hasPrevious()) {
                this.getPrevious().next = null;
            }
            // set link to new predecessor
            this.previous = previous;
            // construct new predecessor's next link
            if (this.hasPrevious() && this.getPrevious().getNext() != this) {
                this.getPrevious().setNext(this);
            }
        }

        public Element getNext() {
            return next;
        }

        /**
         * Set the next element link. The provided element is allowed to be null, which
         * effectively appointing this element to be the tail.
         *
         * @param next the element to be the successor of this element, or NULL.
         */
        public void setNext(Element next) {
            // break the current successor's previous link to this
            if (this.hasNext()) {
                this.getNext().previous = null;
            }
            // set link to new successor
            this.next = next;
            // construct new successor's previous link
            if (this.hasNext() && this.getNext().getPrevious() != this) {
                this.getNext().setPrevious(this);
            }
        }

        public boolean hasNext() {
            return this.next != null;
        }

        public boolean hasPrevious() {
            return this.previous != null;
        }
    }
}