import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * A Page/Buffer of {@link Measurement} data. This is the holding
 * structure when returning read function results to user. Note that
 * this class is actually an Array List ADT.
 *
 * Since we only needed limited functions of Array List, we do not bother
 * to implement remove methods. And the add methods only fills up from left
 * of right.
 *
 * @author Weinan Jimmy Michael
 */
public class Page implements Renderable {

    // track which index which be filled in the array
    private int currentIndex;

    // the array of data
    private Measurement[] data;

    /**
     * Initialize the page with a given size
     *
     * @param pageSize
     */
    public Page(int pageSize) {
        this.currentIndex = 0;
        this.data = new Measurement[pageSize];
    }

    /**
     * Add one item to the page. It fills up the next
     * vacant spot. It does nothing if data is about to
     * overflow. This is okay since we are only using
     * this in very controlled scenarios.
     *
     * @param elem
     */
    public void add(Measurement elem) {
        if (this.currentIndex >= this.data.length)
            return;
        this.data[this.currentIndex] = elem;
        this.currentIndex++;
    }

    /**
     * Get the item at the specified index.
     *
     * @param pos
     * @return
     */
    public Measurement get(int pos) {
        return this.data[pos];
    }

    /**
     * Return the (actual) size of this array. It would be the current filling
     * index, capped by the page size (array length).
     *
     * @return
     */
    public int getSize() {
        return Math.min(this.currentIndex, this.data.length);
    }

    /**
     * User facing string representation of this page of data.
     *
     * @return
     */
    @Override
    public String render() {
        return Arrays.stream(this.data)
                .filter(m -> m != null)
                .map(Measurement::render)
                .collect(Collectors.joining("\n"));
    }
}
