/**
 * Interface for things that is presentable to the user, either through
 * API itself or through the command line user interface.
 *
 * @author Weinan Jimmy Michael
 */
public interface Renderable {

    /**
     * Render to string format so it can be printed.
     * @return
     */
    String render();
}
