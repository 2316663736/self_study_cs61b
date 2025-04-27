package deque;
import java.util.Iterator;
public class LinkedListDeque<T> {
    private int size;
    //head 和tail 始终指向固定的元素（即浪费两个元素空间，以求head和tail 不变）
    private final Node head;
    private final Node tail;

    private class Node {
        public T num;
        public Node next;
        public Node prev;
        public Node(T n) {
            num = n;
        }
        public Node(T n, Node p, Node q) {
            num = n;
            prev = p;
            next = q;
        }

    }

    public LinkedListDeque() {
        head = new Node(null);
        tail = new Node(null);
        head.next = tail;
        tail.prev = head;
        size=0;
    }

    public void addFirst(T item){
        Node tem=new Node(item,head,head.next);
        head.next.prev=tem;
        head.next=tem;
        size+=1;
    }

    public void addLast(T item){
        Node tem=new Node(item,tail.prev,tail);
        tail.prev.next=tem;
        tail.prev=tem;
        size+=1;
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

    public T removeFirst(){
        if (size==0)
            return null;
        T item = head.next.num;
        head.next=head.next.next;
        head.next.prev=head;
        size-=1;
        return item;
    }

    public T removeLast(){
        if(size==0)
            return null;
        T item = tail.prev.num;
        tail.prev=tail.prev.prev;
        tail.prev.next=tail;
        size-=1;
        return item;
    }

    public T get(int index){
        if (index<0 || index>=size)
            return null;
        Iterator<T> itr=new LinkedListIterator();
        T item=null;
        while(index>=0) {
            item = itr.next();
            index-=1;
        }
        return item;
    }


    private T Recursion_get_index(Node n,int index){
        if (index==0)
            return n.num;
        return Recursion_get_index(n.next,index-1);
    }
    public T getRecursive(int index){
        if (index<0 || index>=size)
            return null;
        return Recursion_get_index(head.next,index);
    }

    private class LinkedListIterator implements Iterator<T>{
        Node current ;
        public LinkedListIterator() {
            current=head.next;
        }
        public boolean hasNext() {
            return current!=tail;
        }
        public T next() {
            T item=current.num;
            current=current.next;
            return item;
        }
    }

    public Iterator<T> iterator(){
        return new LinkedListIterator();
    }

    public boolean equals(Object o){
        if (!(o instanceof LinkedListDeque))
            return false;
        if(((LinkedListDeque<?>) o).size()!=this.size())
            return false;

        if(this.isEmpty())
            return true;
        Iterator<?> itr1= ((LinkedListDeque<?>) o).iterator();
        Iterator<T> itr2=this.iterator();
        while(itr1.hasNext() && itr2.hasNext()) {
            if(!itr1.next().equals(itr2.next()))
                return false;
        }

        return true;
    }
}
