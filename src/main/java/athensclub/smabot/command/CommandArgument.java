package athensclub.smabot.command;

import athensclub.smabot.SMABotUserException;
import athensclub.smabot.SMABotUtil;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.user.User;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * A class that hold a single command argument. It can convert the value to a lot of types and
 * throw {@link SMABotUserException} when it fails to do so.
 */
public class CommandArgument {

    private final String value;

    private final DiscordApi api;

    public CommandArgument(String value, DiscordApi api) {
        this.value = value;
        this.api = api;
    }


    public Optional<User> asUserOptional() {
        try {
            return Optional.of(asUser());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public User asUser() {
        return SMABotUtil.getMemberFromMention(value, api);
    }

    public OptionalInt asIntOptional() {
        try {
            return OptionalInt.of(Integer.parseInt(value));
        } catch (Exception e) {
            return OptionalInt.empty();
        }
    }

    public int asInt() {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new SMABotUserException("Expected Integer, found: " + value);
        }
    }

    public OptionalDouble asDoubleOptional(){
        try{
            return OptionalDouble.of(Double.parseDouble(value));
        }catch(Exception e){
            return OptionalDouble.empty();
        }

    }
    public double asDouble(){
        try{
            return Double.parseDouble(value);
        }catch(NumberFormatException e){
            throw new SMABotUserException("Expected decimal value, found: " + value);
        }
    }

    public String getValue() {
        return value;
    }
}
