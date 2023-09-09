package pe.org.jhsystem.demos.chat.telegram.bot.handlers;

import java.util.Map;

import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import static pe.org.jhsystem.demos.chat.telegram.bot.constants.Constants.START_TEXT;
import static pe.org.jhsystem.demos.chat.telegram.bot.constants.UserState.*;

import pe.org.jhsystem.demos.chat.telegram.bot.constants.Constants;
import pe.org.jhsystem.demos.chat.telegram.bot.constants.UserState;
import pe.org.jhsystem.demos.chat.telegram.bot.factories.KeyboardFactory;

public class ResponseHandler {
    private final SilentSender sender;
    private final Map<Long, UserState> chatStates;

    public ResponseHandler(SilentSender sender, DBContext db) {
        this.sender = sender;
        chatStates = db.getMap(Constants.CHAT_STATES);
    }

    public void replyToStart(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(START_TEXT);
        sender.execute(message);
        chatStates.put(chatId, AWAITING_NAME);
    }

	public void replyToButtons(long chatId, Message message) {
	    if (message.getText().equalsIgnoreCase("/stop")) {
	        stopChat(chatId);
	    }
	
	    switch (chatStates.get(chatId)) {
	        case AWAITING_NAME -> replyToName(chatId, message);
	        case FOOD_DRINK_SELECTION -> replyToFoodDrinkSelection(chatId, message);
	        case PIZZA_TOPPINGS -> replyToPizzaToppings(chatId, message);
	        case AWAITING_CONFIRMATION -> replyToOrder(chatId, message);
	        default -> unexpectedMessage(chatId);
	    }
	}

    private void unexpectedMessage(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("No esperaba eso.");
        sender.execute(sendMessage);
    }

	private void stopChat(long chatId) {
	    SendMessage sendMessage = new SendMessage();
	    sendMessage.setChatId(chatId);
	    sendMessage.setText("Gracias por su orden. ¡Nos vemos pronto!\nPresionar /start para ordenar otra vez");
	    chatStates.remove(chatId);
	    sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
	    sender.execute(sendMessage);
	}

    private void replyToOrder(long chatId, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        if ("si".equalsIgnoreCase(message.getText())) {
            sendMessage.setText("Lo entregaremos pronto. ¡Gracias!\n¿Pedir otro?");
            sendMessage.setReplyMarkup(KeyboardFactory.getPizzaOrDrinkKeyboard());
            sender.execute(sendMessage);
            chatStates.put(chatId, FOOD_DRINK_SELECTION);
        } else if ("no".equalsIgnoreCase(message.getText())) {
            stopChat(chatId);
        } else {
            sendMessage.setText("Por favor seleccionar si o no");
            sendMessage.setReplyMarkup(KeyboardFactory.getYesOrNo());
            sender.execute(sendMessage);
        }
    }

    private void replyToPizzaToppings(long chatId, Message message) {
        if ("americana".equalsIgnoreCase(message.getText())) {
            promptWithKeyboardForState(chatId, "Has seleccionado Pizza Americana.\nLo entregaremos pronto. ¡Gracias!\n¿Hacer el pedido de nuevo?",
                    KeyboardFactory.getYesOrNo(), AWAITING_CONFIRMATION);
        } else if ("peperoni".equalsIgnoreCase(message.getText())) {
            promptWithKeyboardForState(chatId, "Nosotros terminamos la Pizza Peperoni.\nSeleccione otro ingrediente",
                    KeyboardFactory.getPizzaToppingsKeyboard(), PIZZA_TOPPINGS);
        } else {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("Nosotros no vendemos Pizza" + message.getText() + ".\nSelecciona los ingredientes!");
            sendMessage.setReplyMarkup(KeyboardFactory.getPizzaToppingsKeyboard());
            sender.execute(sendMessage);
        }
    }

    private void promptWithKeyboardForState(long chatId, String text, ReplyKeyboard YesOrNo, UserState awaitingReorder) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(YesOrNo);
        sender.execute(sendMessage);
        chatStates.put(chatId, awaitingReorder);
    }

	private void replyToFoodDrinkSelection(long chatId, Message message) {
	    SendMessage sendMessage = new SendMessage();
	    sendMessage.setChatId(chatId);
	    if ("bebida".equalsIgnoreCase(message.getText())) {
	        sendMessage.setText("Nosotros no vendemos bebidas.\nTrae tu propia bebida!! :)");
	        sendMessage.setReplyMarkup(KeyboardFactory.getPizzaOrDrinkKeyboard());
	        sender.execute(sendMessage);
	    } else if ("pizza".equalsIgnoreCase(message.getText())) {
	        sendMessage.setText("Nos encanta la pizza aquí.\n¡Selecciona los ingredientes!");
	        sendMessage.setReplyMarkup(KeyboardFactory.getPizzaToppingsKeyboard());
	        sender.execute(sendMessage);
	        chatStates.put(chatId, UserState.PIZZA_TOPPINGS);
	    } else {
	        sendMessage.setText("Nosotros no vendemos " + message.getText() + ". Por favor seleccione entre las siguientes opciones.");
	        sendMessage.setReplyMarkup(KeyboardFactory.getPizzaOrDrinkKeyboard());
	        sender.execute(sendMessage);
	    }
	}

	private void replyToName(long chatId, Message message) {
	    promptWithKeyboardForState(chatId, "Hola " + message.getText() + ". ¿Qué te gustaría?",
	      KeyboardFactory.getPizzaOrDrinkKeyboard(),
	      UserState.FOOD_DRINK_SELECTION);
	}

    public boolean userIsActive(Long chatId) {
        return chatStates.containsKey(chatId);
    }
}
