package deque;

import java.util.Comparator;
import java.util.Iterator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private final Comparator<T> comp;
    public MaxArrayDeque(Comparator<T> c) {
        super();
        comp = c;
    }


    public T max() {
        return getT((Comparator<T>) comp);
    }



    public T max(Comparator<T> c){
        return getT((Comparator<T>) c);
    }

    private T getT(Comparator<T> c) {
        if(isEmpty()) {
            return null;
        }
        Iterator<T> iter = iterator();
        T now_max = iter.next();
        while(iter.hasNext()) {
            T next = iter.next();
            if(c.compare(next, now_max) > 0) {
                now_max = next;
            }
        }
        return now_max;
    }
}
