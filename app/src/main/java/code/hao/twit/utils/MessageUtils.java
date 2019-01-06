package code.hao.twit.utils;

import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MessageUtils {
    public static int MAX_LENGTH = 50;

    public static List<String> splitMessage(String message, int characterLimit) throws SplitMessageException {
        message = message.trim(); // obviously

        List<String> messageDataList = new ArrayList<>();

        if (TextUtils.isEmpty(message)) {
            return messageDataList;
        }

        // if length of message is less than character limit, return the message
        if (message.length() <= characterLimit) {
            messageDataList.add(message);
            return messageDataList;
        }

        // total part of the message
        int totalPart = (message.length() / characterLimit) + (message.length() % characterLimit == 0 ? 0 : 1);

        String[] words = message.split("\\s");
        for (String word : words) {
            if (word.length() > characterLimit) {
                throw new SplitMessageException(SplitMessageException.WORD_LENGTH_OVER_LIMIT_CHARACTER);
            }
        }

        return separateMultipart(words, totalPart, characterLimit);
    }

    private static List<String> separateMultipart(String[] words, int totalParts, int characterLimit) throws SplitMessageException{
        List<String> messageDataList = new ArrayList<>();
        int breakAtIndex = -1;

        for (int partIndex = 0; partIndex < totalParts; partIndex++) {
            StringBuilder partial = new StringBuilder();
            partial.append(partIndex + 1).append("/").append(totalParts).append(" ");//append indicator
            String indicator = partial.toString();
            int length = partial.length();
            int indicatorLength = partial.length();

            for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
                String word = words[wordIndex];
                // if length of each word include indicator length greater than character limit, return empty array
                // is this logic ok? need to check? => BTW: this case is rare. but, still need to handle?
                // with "well, whoamiwhatiamgonnadotheworldissomuchmorepainful" => error
                // expected result: "1/2 well," + "2/2 whoamiwhatiamgonnadotheworldissomuchmorepainful" ...?
                // => then the latter is > 50 even though the origin is < 50.. =>
                // => + Due to the requirement => return error for this case.
                if (word.length() + indicatorLength > characterLimit) {
                    break;
                }

                // skip words that already joined into each part
                if (wordIndex <= breakAtIndex) {
                    continue;
                }

                // Pre-caculate partial length before joined (+ 1 because of a suffix whitespace per word)
                length += word.length() + 1;

                // Break loop if length is over than limit, (+ 1 because the last whitespace before trimming)
                if (length > characterLimit + 1) {
                    break;
                }

                //Append individual word after prefix indicator
                partial.append(word).append(" ");

                //Store current index where the last word is appended
                breakAtIndex = wordIndex;
            }

            // beak loop if indicator string and partial string same value
            if (indicator.equals(partial.toString())) {
                messageDataList.clear();
                throw new SplitMessageException(SplitMessageException.INDICATOR_SAME_PARTIAL_VALUE);
            }

            messageDataList.add(partial.toString().replaceAll("\\s+$", ""));  //to trim trailing whitespace.
            partial.setLength(0);
        }

        //Applied recursive to re-calculate total partial
        if (breakAtIndex < words.length - 1) {
            messageDataList.clear();
            messageDataList = separateMultipart(words, totalParts + 1, characterLimit);
        }

//        for (int i = 0; i < messageDataList.size(); i++) {
//            System.out.println(messageDataList.get(i));
//            System.out.println(messageDataList.get(i).length());
//
//        }

        return messageDataList;
    }

    /**
     * {@link SplitMessageException}
     */
    public static final class SplitMessageException extends Exception {
        public static final String WORD_LENGTH_OVER_LIMIT_CHARACTER_STRING = "The message contains a span of non-whitespace characters longer than MAX_LENGTH";
        public static final String INDICATOR_SAME_PARTIAL_VALUE_STRING = "The message contains a span of non-whitespace characters and indicator longer than MAX_LENGTH";


        public static final int NON_WHITESPACE_LIMIT_CHARACTER = -123;

        public static final int WORD_LENGTH_OVER_LIMIT_CHARACTER = -124;

        public static final int INDICATOR_SAME_PARTIAL_VALUE = -125;

        private int mErrorCode;

        public SplitMessageException(int errorCode) {
            mErrorCode = errorCode;
        }

        public int getErrorCode() {
            return mErrorCode;
        }

        public void setErrorCode(int errorCode) {
            mErrorCode = errorCode;
        }

        @Override
        public void printStackTrace() {
            if (getErrorCode() == WORD_LENGTH_OVER_LIMIT_CHARACTER || getErrorCode() == NON_WHITESPACE_LIMIT_CHARACTER) {
                System.err.println(WORD_LENGTH_OVER_LIMIT_CHARACTER_STRING);
            } else if (getErrorCode() == INDICATOR_SAME_PARTIAL_VALUE) {
                System.err.println(INDICATOR_SAME_PARTIAL_VALUE_STRING);
            }
        }

        @Override
        public String getMessage() {
            if (getErrorCode() == WORD_LENGTH_OVER_LIMIT_CHARACTER || getErrorCode() == NON_WHITESPACE_LIMIT_CHARACTER) {
                return WORD_LENGTH_OVER_LIMIT_CHARACTER_STRING;
            } else if (getErrorCode() == INDICATOR_SAME_PARTIAL_VALUE) {
                return INDICATOR_SAME_PARTIAL_VALUE_STRING;
            }
            return super.getMessage();
        }
    }
}
