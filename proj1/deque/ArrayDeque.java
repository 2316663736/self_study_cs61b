package deque;
import java.util.Iterator;
public class ArrayDeque<T> {
    private T[] array;
    private int size;

    private int now_len;

    private int min_size;
    private double small_rate;
    private double expand_rate;
    private int min_expand_n;

    public ArrayDeque(){
        size=0;
        now_len=8;
        array = (T[]) new Object[now_len];
        min_size=16;
        small_rate=0.25;
        expand_rate=1.02;
        min_expand_n=2;
    }

    private void reszie(int new_size){
        T[] new_array=(T[]) new Object[new_size];
        System.arraycopy(array, 0, new_array, 0, size);
        array=new_array;
        now_len=new_size;
    }

    private void expand_size(){
        if(size==now_len)
            reszie(Math.max((int)Math.round(now_len*expand_rate),min_expand_n+now_len));
    }

    public void addFirst(T item){
        expand_size();
        for(int i=0;i<size;i++)
            array[i+1]=array[i];

        size++;
        array[0]=item;
    }

    public void addLast(T item){
        expand_size();
        array[size]=item;
        size++;
    }

    public boolean isEmpty(){
        return size==0;
    }


    public int size(){
        return size;
    }

    public void printDeque(){
        Iterator<T> itr =iterator();
        while(itr.hasNext())
            System.out.println(itr.next()+" ");

        System.out.println();
    }
    private void small_size(){
        if(now_len>=min_size&&size<=(int)now_len*small_rate)
            reszie((int)(now_len*small_rate));
    }
    public T removeFirst(){
        if(isEmpty())
            return null;
        T item=array[0];
        for(int i=1;i<size;i++)
            array[i-1]=array[i];
        array[size-1]=null;
        size--;
        small_size();
        return item;
    }

    public T removeLast(){
        if(isEmpty())
            return null;
        T item=array[size-1];
        array[size-1]=null;
        size--;
        small_size();
        return item;
    }

    public T get(int index){
        if(index<0 || index>=size)
            return null;
        return array[index];
    }

    private class ArrayDequeIterator implements Iterator<T>{
        int now;
        public ArrayDequeIterator(){
            now=0;
        }
        public boolean hasNext(){
            return now<size;
        }
        public T next(){
            T item=array[now];
            now++;
            return item;
        }
    }

    public Iterator<T> iterator(){
        return new ArrayDequeIterator();
    }

    public boolean equals(Object o){
        if (!(o instanceof ArrayDeque))
            return false;
        if(((ArrayDeque<?>) o).size()!=this.size())
            return false;

        if(this.isEmpty())
            return true;
        Iterator<?> itr1= ((ArrayDeque<?>) o).iterator();
        Iterator<T> itr2=this.iterator();
        while(itr1.hasNext() && itr2.hasNext()) {
            if(!itr1.next().equals(itr2.next()))
                return false;
        }

        return true;
    }
}
