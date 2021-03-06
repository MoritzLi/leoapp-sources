package de.slg.messenger;

public class Chat {

    public int chatId;
    public String chatName;
    public final Chattype chatTyp;
    public Message letzeNachricht;
    public String chatVisibleName;

    public Chat(int chatId, String chatName, Chattype chatTyp) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.chatTyp = chatTyp;
    }

    @Override
    public String toString() {
        return "id: " + chatId + ", name: " + chatName + ", typ: " + chatTyp.toString();
    }

    @Override
    public boolean equals(Object o) {
        Chat c = null;
        if (o instanceof Chat)
            c = (Chat) o;
        if (c == null || chatTyp.compareTo(Chattype.PRIVATE) != 0 || c.chatTyp.compareTo(Chattype.PRIVATE) != 0)
            return chatId == c.chatId;
        String[] s1 = c.toString().split(" - ");
        String[] s2 = toString().split(" - ");
        if (s1.length == 2 && s2.length == 2)
            return (s1[0].equals(s2[0]) && s1[1].equals(s2[1])) || (s1[0].equals(s2[1]) && s1[1].equals(s2[0]));
        return false;
    }

    public boolean allAttributesSet() {
        return chatId > 0 && chatName != null && chatTyp != null;
    }

    public void letzteNachricht(Message m) {
        if (m != null)
            letzeNachricht = m;
    }

    public enum Chattype {
        PRIVATE, GROUP
    }
}