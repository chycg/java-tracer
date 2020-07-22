package com.ali.trace.agent.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.ali.trace.agent.util.TreeNode;

import javafx.util.Pair;

/**
 * @author nkhanlang@163.com
 */
public class NodePool {

    private static final AtomicLong MAX = new AtomicLong(0L);
    private static final AtomicLong MIN = new AtomicLong(0L);
    private static final NodePool INSTANCE = new NodePool();

    private final Map<Long, TreeNode> POOL = new ConcurrentHashMap<Long, TreeNode>();
	private final LinkedBlockingQueue<Pair<Long, Long>> QUEUE = new LinkedBlockingQueue<>();

    private volatile long size = 5;
    public static NodePool getPool() {
        return INSTANCE;
    }

    public TreeNode getNode(Long seed){
        return POOL.get(seed);
    }

    public void setSize(long size){
        this.size = size;
        while(MAX.get() - MIN.get() > size) {
            Pair<Long, Long> expire = QUEUE.poll();
            if( expire != null){
                POOL.remove(expire.getKey());
                MIN.incrementAndGet();
            }else{
                break;
            }
        }
    }
    public long getSize(){return size;}

    public Map<Long, Long> getNodes(){
        Map<Long, Long> map = new HashMap<Long, Long>();
        Iterator<Pair<Long, Long>> itr = QUEUE.iterator();
        while(itr.hasNext()){
            Pair<Long, Long> pair = itr.next();
            map.put(pair.getKey(), pair.getValue());
        }
        return map;
    }

    public void addNode(TreeNode node){
        long seed = MAX.incrementAndGet();
        while(seed - MIN.get() > size) {
            Pair<Long, Long> expire = QUEUE.poll();
            if( expire != null){
                POOL.remove(expire.getKey());
                MIN.incrementAndGet();
            }else{
                break;
            }
        }
        POOL.put(seed, node);
        QUEUE.offer(new Pair<Long, Long>(seed, node.getId()));
    }

}
