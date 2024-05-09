import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

class ChatServiceTest {

    @Test
    fun sendMessageById() {
        val chatService = ChatService()

        val newMessage = Message(ownerNameMessage = "Ben", text = "First message for Ben!")
        val newChatWithMessage = chatService.sendMessageById(1, newMessage)

        assertEquals(newChatWithMessage, chatService.getChats().first())
    }

    @Test
    fun sendMessageByName() {
        val chatService = ChatService()
        chatService.addChat("Alice")
        val newMessage = Message(ownerNameMessage = "By Alice", text = "First message for Ben!")
        val newChatWithMessage = chatService.sendMessageByName("Alice", newMessage)

        assertEquals(newChatWithMessage, chatService.getChats().first())
        assertEquals("Alice", newChatWithMessage.ownerName)
        assertNotEquals("NotAlice", newChatWithMessage.ownerName)
    }

    @Test
    fun `getUnreadChatsCount returns a list of chats with unread messages`() {
        val chatService = ChatService()
        val chat1 = chatService.sendMessageById(1, Message(ownerNameMessage = "Alice", text = "Hi"))
        val chat2 = chatService.sendMessageById(1, Message(ownerNameMessage = "Bob", text = "Hello"))
        chat2.messages.first().isReadMessage = true
        chat2.updateChatStatusToRead()
        val unreadChats = chatService.getUnreadChatsCount()
        assertEquals(1, unreadChats.size)
        assertEquals(chat1, unreadChats.first())
    }


    @Test
    fun `getLastMessages returns the last n messages from a chat`() {
        val chatService = ChatService()
        chatService.sendMessageById(4, Message(ownerNameMessage = "Alice", text = "Hi"))
        chatService.sendMessageById(4, Message(ownerNameMessage = "Alice", text = "How are you?"))
        chatService.sendMessageById(4, Message(ownerNameMessage = "Bob", text = "I'm good"))

        val lastMessages = chatService.getLastMessages(chatIdSearch = 4, limit = 2)

        assertEquals(2, lastMessages?.size)
        assertEquals("I'm good", lastMessages?.last()?.text)
        assertEquals("How are you?", lastMessages?.first()?.text)
    }

    @Test
    fun `createMessage creates a new message`() {
        val chatService = ChatService()
        val message = chatService.createMessage("Hello", "Alice")

        assertEquals("Hello", message?.text)
        assertEquals("Alice", message?.ownerNameMessage)
    }


    @Test
    fun `getAllMessagesFromChat returns all messages from a chat`() {
        val chatService = ChatService()
        chatService.sendMessageById(2, Message(ownerNameMessage = "Alice", text = "Hi"))
        chatService.sendMessageById(2, Message(ownerNameMessage = "Bob", text = "Hello"))

        val allMessages = chatService.getAllMessagesFromChat(chatIdSearch = 2)

        assertEquals(2, allMessages?.size)
        assertEquals("Hi", allMessages?.first()?.text)
        assertEquals("Hello", allMessages?.last()?.text)
    }

    @Test
    fun `getChats returns a list of all chats`() {
        val chatService = ChatService()
        val chat1 = chatService.sendMessageById(1, Message(ownerNameMessage = "Alice", text = "Hi"))
        val chat2 = chatService.sendMessageById(2, Message(ownerNameMessage = "Bob", text = "Hello"))

        val chats = chatService.getChats()

        assertEquals(2, chats.size)
        assertTrue(chats.contains(chat1))
        assertTrue(chats.contains(chat2))
    }

    @Test
    fun `deleteChat marks a chat as deleted`() {
        val chatService = ChatService()
        val chat = chatService.sendMessageById(1, Message(ownerNameMessage = "Alice", text = "Hi"))

        assertTrue(chatService.deleteChat(chat.chatId))
        assertTrue(chat.isDeletedChat)
    }

    @Test
    fun `deleteChat returns false`() {
        val chatService = ChatService()
        assertFalse(chatService.deleteChat(1))
    }

    @Test
    fun `deleteMessage marks a message as deleted`() {
        val chatService = ChatService()
        val chat = chatService.sendMessageById(1, Message(ownerNameMessage = "Alice", text = "Hi"))
        val messageId = chat.messages.first().messageId

        assertTrue(chatService.deleteMessage(chat.chatId, messageId))
        assertTrue(chat.messages.first().isDeletedMessage)
    }

    @Test
    fun `deleteMessage returns false`() {
        val chatService = ChatService()
        assertFalse(chatService.deleteMessage(1, 1))
    }
}