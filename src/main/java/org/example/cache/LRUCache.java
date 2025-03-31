package org.example.cache;

import java.util.*;

public class LRUCache {
    static class Node {
        int key, value;
        Node prev, next;

        Node(int _key, int _value){
            key = _key;
            value = _value;
        }
    }

    private final int capacity;
    private final Map<Integer, Node> cache;
    private final Node head, tail;

    public LRUCache(int capacity){
        this.capacity = capacity;
        cache = new HashMap<>();
        head = new Node(-1, -1);
        tail = new Node(-1, -1);
        tail.prev = head;
        head.next = tail;
    }

    private void addNode(Node node){
        Node temp = head.next;
        head.next = node;
        node.prev = head;
        node.next = temp;
        temp.prev = node;
    }

    private void removeNode(Node node){
        Node prevNode = node.prev;
        Node nextNode = node.next;
        prevNode.next = nextNode;
        nextNode.prev = prevNode;
    }

    public int getKey(int key){
        if(!cache.containsKey(key))
            return -1;

        Node node = cache.get(key);
        removeNode(node);
        addNode(node);
        return node.value;
    }

    public void put(int key, int value){
        if (cache.containsKey(key)) {
            Node existingNode = cache.get(key);
            removeNode(existingNode);
            cache.remove(key); 
        }
        if(cache.size() == capacity){
            cache.remove(tail.prev.key);
            removeNode(tail.prev);
        }
        Node newNode = new Node(key, value);
        addNode(newNode);
        cache.put(key, newNode);
    }
}