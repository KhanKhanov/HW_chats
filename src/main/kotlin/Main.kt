data class Chat(
    var ownerName: String = "user",
    var isDeletedChat: Boolean = false,
    var messages: MutableList<Message> = mutableListOf(),
    var isReadChat: Boolean = false
) {
    val chatId: Int
    val ownerId: Int

    companion object {
        private var chatIdCounter = 0
        private var ownerIdCounter = 0
    }

    private fun generateChatId(): Int {
        return ++chatIdCounter
    }

    private fun generateOwnerId(): Int {
        return ++ownerIdCounter
    }

    init {
        chatId = generateChatId()
        ownerId = generateOwnerId()
    }

    fun updateChatStatusToRead() {      // Проверка для чата, о наличии хотя бы одного непрочитанного сообщения.
        isReadChat = messages.any { it.isReadMessage }

    }
}

data class Message(
    var ownerNameMessage: String = "",
    var text: String = "New message",
    var isReadMessage: Boolean = false,
    var isDeletedMessage: Boolean = false
) {
    val messageId: Int
    val ownerIdMessage: Int

    companion object {
        private var messageIdCounter = 0
        private var ownerIdMessageCounter = 0
    }

    private fun generateMessageId(): Int {
        return ++messageIdCounter
    }

    private fun generateOwnerIdMessage(): Int {
        return ++ownerIdMessageCounter
    }

    init {
        messageId = generateMessageId()
        ownerIdMessage = generateOwnerIdMessage()
    }

}

class ChatService {
    private val directChat: MutableList<Chat> = mutableListOf()

    fun addChat(nameUserByChat: String): Chat {
        val newChat = Chat(ownerName = nameUserByChat)
        return directChat.add(newChat).run { newChat }
    }

    fun sendMessageById(userId: Int, newMessage: Message = Message(text = "")): Chat {
        return directChat.find { it.ownerId == userId && !it.isDeletedChat }?.apply {
            // apply - возвращает объект, над которым выполнены операции
            messages.add(newMessage)
            updateChatStatusToRead()
        } ?: run {  // run - возвращает результат выполнения блока кода
            val newChat = Chat().also { it.messages.add(newMessage) }   // also - возвращает ссылку на объект
            directChat.add(newChat)
            newChat
        }
    }

    fun sendMessageByName(userName: String, newMessage: Message = Message(text = "")): Chat {
        return directChat.find { it.ownerName == userName && !it.isDeletedChat }?.apply {
            messages.add(newMessage)
            updateChatStatusToRead()
        } ?: run {
            val newChat = Chat().also {
                it.ownerName = userName
                it.messages.add(newMessage)
            }
            directChat.add(newChat)
            newChat
        }
    }

    fun getUnreadChatsCount(): List<Chat> {     // получить список чатов с непрочитанными сообщениями (только те, у которых есть хотя бы одно непрочитанное сообщение)
        val unreadChats = directChat.filter { !it.isReadChat }
        println(unreadChats.joinToString (" -> ", "All UNread chats:\n -> ") {
            "Id unread chat ${it.chatId}\n"
        } )
        return unreadChats
    }

    fun getLastMessages(chatIdSearch: Int? = null, ownerIdSearch: Int? = null, ownerNameSearch: String? = null, limit: Int = 5): List<Message>? {     // получение последних пяти сообщений
        val chat = directChat.find {
            (chatIdSearch == null || it.chatId == chatIdSearch && !it.isDeletedChat) &&
                    (ownerIdSearch == null || it.ownerId == ownerIdSearch && !it.isDeletedChat) &&
                    (ownerNameSearch == null || it.ownerName == ownerNameSearch && !it.isDeletedChat)
        }
        val lastMessages = chat?.messages?.filter { !it.isDeletedMessage }
            ?.takeLast(limit)
            ?.onEach { it.isReadMessage = true }
            ?: run {
                println("No messages!")
                null
            }
        chat?.updateChatStatusToRead()
        println(lastMessages?.joinToString(prefix = "The last $limit messages:\n", separator = "") {
            "\tMessage from user = '${it.ownerNameMessage}' with ownerID = ${it.ownerIdMessage} and chatID = ${chat?.chatId} about: \n" +
                    "\t-> text = '${it.text}'\n "
        } )
        return lastMessages
    }

    fun createMessage(textIn: String, ownerName: String? = null): Message? {   // создать новое сообщение, но без обязательной отправки
        return textIn.let { ownerName?.let { it1 -> Message(text = it, ownerNameMessage = it1) } }
    }

    fun getAllMessagesFromChat(chatIdSearch: Int? = null, ownerIdSearch: Int? = null, ownerNameSearch: String? = null): List<Message>? {  // получить список всех сообщений из чата
        val chat = directChat.find {
            (chatIdSearch == null || it.chatId == chatIdSearch && !it.isDeletedChat) &&
                    (ownerIdSearch == null || it.ownerId == ownerIdSearch && !it.isDeletedChat) &&
                    (ownerNameSearch == null || it.ownerName == ownerNameSearch && !it.isDeletedChat)
        }
        val allMessages = chat?.messages?.filter { !it.isDeletedMessage }
            ?.onEach { it.isReadMessage = true }
            ?: run {
                println("No messages!")
                null
            }
        chat?.updateChatStatusToRead()
        println(allMessages?.joinToString (prefix = "All messages in the chat with id ${chat?.chatId}:\n", separator = "") {
            "-> ID = ${it.messageId}, text = ${it.text}\n"
        } )
        return allMessages
    }

    fun getChats(): List<Chat> { // получить список чатов (ВСЕХ)
        println(directChat.joinToString (prefix = "All chats:\n", separator = "") {
            "-> chat id ${it.chatId}\n"
        } )
        return directChat
    }

    fun deleteChat (chatIdSearch: Int): Boolean { // удалить конкретный чат
        return directChat.find { it.chatId == chatIdSearch && !it.isDeletedChat }
            ?.also { it.isDeletedChat = true }
            ?.let { true }
            ?: false
    }

    fun deleteMessage(chatIdSearch: Int, messageIdSearch: Int): Boolean {   // удалить конкретное сообщение
        return directChat.find { it.chatId == chatIdSearch && !it.isDeletedChat }
            ?.apply {
                messages.find { it.messageId == messageIdSearch && !it.isDeletedMessage }
                    ?.also { it.isDeletedMessage = true }
            }
            ?.let { true }
            ?: false
    }
}

fun main() {
    val chatService = ChatService()
    val newMessage = Message(text = "new Message # 1")
    val newMessage2 = Message(text = "new Message # 2")

    chatService.sendMessageById(1, newMessage)
    chatService.sendMessageById(1, newMessage2)
    chatService.sendMessageById(2, newMessage)
    chatService.createMessage(textIn = "new Text")?.let { chatService.sendMessageById(userId = 4) }

    println("First command")
    chatService.getAllMessagesFromChat(1)
    println("Second command")
    chatService.getChats()


    chatService.getUnreadChatsCount()
    val lastMess = chatService.getLastMessages(1)
    println(lastMess?.size)

//    chatService.getUnreadChatsCount()
//    chatService.getLastMessages(1)
//    chatService.getUnreadChatsCount()
//
//    println("\nCheck for deleted message\n")
//    chatService.getAllMessagesFromChat(1)
//    chatService.deleteMessage(1, 1)
//    println("After deleted message # 1 from chat ID = # 1\n")
//    chatService.getAllMessagesFromChat(1)

}
