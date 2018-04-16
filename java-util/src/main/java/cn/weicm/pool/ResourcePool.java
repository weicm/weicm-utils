package cn.weicm.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * TODO 待完善
 * <p>Author: weicm</p>
 * <p>Date: 2018/1/29 10:27</p>
 * <p>Desp: 资源池</p>
 */
public abstract class ResourcePool<T> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    private volatile Integer curCapacity = 0;
    //资源池容量
    private final Integer capacity;
    private final List<Res<T>> pointer;
    //有界资源队列
    protected final ArrayBlockingQueue<Res<T>> queue;
    //初始化资源池
    public ResourcePool(int capacity) {
        this.capacity = capacity;
        pointer = new ArrayList<Res<T>>(capacity);
        this.queue = new ArrayBlockingQueue<Res<T>>(this.capacity, false);
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/1/29 10:28</p>
     * <p>Desp: 生成新的资源</p>
     * @return
     */
    abstract protected T newResouce();

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/1/29 10:28</p>
     * <p>Desp: 关闭资源</p>
     * @param resouce
     */
    abstract protected void closeResource(T resouce);

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/1/29 10:28</p>
     * <p>Desp: 获取资源</p>
     * @return
     */
    protected Res<T> getResouce() {
        Res<T> resouce = null;
        try {
            if (curCapacity < capacity) {
                synchronized (this) {
                    if (curCapacity < capacity) {
                        Res<T> res = new Res<>(this, curCapacity +1);
                        res.rebuild();
                        pointer.add(res);
                        queue.put(res);
                        curCapacity++;
                    }
                }
            }
            resouce = queue.take();
        } catch (Exception e) {
            throw new RuntimeException("Get resource exception!", e);
        }
        return resouce;
    }

    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/1/29 10:29</p>
     * <p>Desp: 获取资源，还给资源池</p>
     * @param resource
     */
    protected void releaseResouce(Res<T> resource) {
        try {
            queue.put(resource);
        } catch (InterruptedException e) {
            throw new RuntimeException("Release resource exception!", e);
        }
    }

    public <R> R exec(Opt<Res<T>, R> opt) throws Exception {
        Res<T> resouce = getResouce();
        R result = opt.option(resouce);
        return result;
    }
    /**
     * <p>Author: weicm</p>
     * <p>Date: 2018/1/29 10:29</p>
     * <p>Desp: 关闭资源池</p>
     */
    public void close(){
        for (Res<T> resource : pointer) {
            closeResource(resource.get());
        }
    }

    public static class Res<T> {
        private T res;
        private ResourcePool<T> pool;
        private HashMap<String, Object> exts = new HashMap<>();
        private final String name;

        public Res(ResourcePool<T> pool, int number) {
            this.pool = pool;
            this.name = "Res-" + number;
        }

        public T get() {
            return res;
        }

        public void rebuild() {
            if (null != res){
                try {
                    pool.closeResource(res);
                    pool.log.info("Close resource success! Res name: " + name + " resource hashcode; " + res.hashCode());
                } catch (Exception e) {
                    pool.log.info("Close resource fail! Res name: " + name + " resource hashcode; " + res.hashCode());
                    throw e;
                }
            }
            try {
                res = pool.newResouce();
                pool.log.info("Create resource success! Res name: " + name + " resource hashcode; " + res.hashCode());
            } catch (Exception e) {
                pool.log.info("Create resource fail! Res name: " + name + " resource hashcode; " + res.hashCode());
                throw e;
            }
            exts = new HashMap<>();
        }

        public void close() {
            pool.releaseResouce(this);
        }

        public HashMap<String, Object> getExts() {
            return exts;
        }

        public String getName() {
            return name;
        }
    }
}
