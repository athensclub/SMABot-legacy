package athensclub.smabot;

/**
 * The exception that will be displayed to the SMABot user (on discord).
 */
public class SMABotUserException extends RuntimeException{

    public SMABotUserException(String message){
        super(message);
    }

}
