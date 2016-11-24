/**
 * A Queue ADT backed by a Linked List.
 *
 * @author Weinan, Jimmy, Michael
 */
public class Queue {

    // Backing storage for this queue
    private LinkedList list;

    public Queue() {
        this.list = new LinkedList();
    }

    /**
     * Add (Offer) an item to the queue. Under the hood, it gets
     * added to the last position of the linked list.
     *
     * @param item the item to be offered to the queue
     */
    public void enqueue(Object item) {
        this.list.add(item, this.list.getSize());
    }

    /**
     * Remove (Poll) an item from the queue. Under the hood, the first
     * item of the linked list is removed and returned.
     *
     * @return the item to be polled from the queue, or NULL if the queue is empty.
     */
    public Object dequeue() {
        Object item = this.list.get(0);
        if (null != item) {
            this.list.remove(0);
        }
        return item;
    }

    /**
     * Get the size of the queue. Helpful in preventing dry drawing.
     * @return the size of the queue.
     */
    public int getSize() {
        return this.list.getSize();
    }
}
