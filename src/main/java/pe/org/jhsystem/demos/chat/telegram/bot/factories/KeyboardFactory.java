package pe.org.jhsystem.demos.chat.telegram.bot.factories;

import java.util.List;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public class KeyboardFactory {
	public static ReplyKeyboard getPizzaToppingsKeyboard() {
	    KeyboardRow row = new KeyboardRow();
	    row.add("Americana");
	    row.add("Peperoni");
	    return new ReplyKeyboardMarkup(List.of(row));
	}

    public static ReplyKeyboard getPizzaOrDrinkKeyboard(){
        KeyboardRow row = new KeyboardRow();
        row.add("Pizza");
        row.add("Bebida");
        return new ReplyKeyboardMarkup(List.of(row));
    }

    public static ReplyKeyboard getYesOrNo() {
        KeyboardRow row = new KeyboardRow();
        row.add("Si");
        row.add("No");
        return new ReplyKeyboardMarkup(List.of(row));
    }
}
