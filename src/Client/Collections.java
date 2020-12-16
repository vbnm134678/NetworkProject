package Client;

import java.util.*;

// UI를 구성하는데 필요한 정보들 모아놓는 클래스
public class Collections {
	
	// 친구 정보 관리
	public HashMap<String, Friend> friends = new HashMap<String, Friend>();
	
	// 채팅방 관리
	public HashMap<String, ChattingRoom> chats = new HashMap<String, ChattingRoom>();
	
	public int getTotalChatNum() {
		int total = 0;
		for (String key : chats.keySet())
			total += chats.get(key).getChatNum();
		return total;
	}
	
	// 채팅방 생성시 참가자로 선택된 친구 모음
	public TreeMap<String, String> selected = new TreeMap<String, String>();
	
	
	/* 집합을 value 기준으로 소팅 해주는 함수 */
	public static <K,V extends Comparable<? super V>>
	SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
	    SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
	        new Comparator<Map.Entry<K,V>>() {
	            @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
	                int res = e1.getValue().compareTo(e2.getValue());
	                return res != 0 ? res : 1;
	            }
	        }
	    );
	    sortedEntries.addAll(map.entrySet());
	    return sortedEntries;
	}
	
}
