package com.teamsystem.plugin;

import java.io.*;

public class TSIDBase64{

    /**
     * This class need not be instantiated, all methods are static.
     */
    private TSIDBase64(){
    }

    /**
	 * Table of the sixty-four characters that are used as
	 * the Base64 alphabet: [A-Za-z0-9+/]
     */
    protected static final byte[] base64Chars = {
		'A','B','C','D','E','F','G','H',
		'I','J','K','L','M','N','O','P',
		'Q','R','S','T','U','V','W','X',
		'Y','Z','a','b','c','d','e','f',
		'g','h','i','j','k','l','m','n',
		'o','p','q','r','s','t','u','v',
		'w','x','y','z','0','1','2','3',
		'4','5','6','7','8','9','+','/',
	};

    /**
	 * Reverse lookup table for the Base64 alphabet.
     * reversebase64Chars[byte] gives n for the nth Base64
     * character or -1 if a character is not a Base64 character.
     */
    protected static final byte[] reverseBase64Chars = new byte[0xff];
    static {
        // Fill in -1 for all characters to start with
        for (int i=0; i<reverseBase64Chars.length; i++){
			reverseBase64Chars[i] = -1;
        }
        // For characters that are base64Chars, adjust
        // the reverse lookup table.
        for (byte i=0; i < base64Chars.length; i++){
			reverseBase64Chars[base64Chars[i]] = i;
        }
    }


    /**
     * Encode a String in Base64.
	 * The String is converted to and from bytes according to the platform's
     * default character encoding.
     * No line breaks or other white space are inserted into the encoded data.
     *
     * @param string The data to encode.
     * @return An encoded String.
     */
    static String encode(String string){
        return new String(encode(string.getBytes()));
    }

    /**
     * Encode a String in Base64.
     * No line breaks or other white space are inserted into the encoded data.
     *
     * @param string The data to encode.
     * @param enc Character encoding to use when converting to and from bytes.
     * @throws UnsupportedEncodingException if the character encoding specified is not supported.
     * @return An encoded String.
     */
    static String encode(String string, String enc) throws UnsupportedEncodingException {
        return new String(encode(string.getBytes(enc)), enc);
    }

    /**
     * Encode bytes in Base64.
     * No line breaks or other white space are inserted into the encoded data.
     *
     * @param bytes The data to encode.
     * @return Encoded bytes.
     */
    public static byte[] encode(byte[] bytes){
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        // calculate the length of the resulting output.
        // in general it will be 4/3 the size of the input
        // but the input length must be divisible by three.
        // If it isn't the next largest size that is divisible
        // by three is used.
        int mod;
        int length = bytes.length;
        if ((mod = length % 3) != 0){
            length += 3 - mod;
        }
        length = length * 4 / 3;
        ByteArrayOutputStream out = new ByteArrayOutputStream(length);
        try {
        	encode(in, out, false);
        } catch (IOException x){
            // This can't happen.
            // The input and output streams were constructed
            // on memory structures that don't actually use IO.
        }
        return out.toByteArray();
    }

    /**
     * Encode data from the InputStream to the OutputStream in Base64.
     *
     * @param in Stream from which to read data that needs to be encoded.
     * @param out Stream to which to write encoded data.
     * @param lineBreaks Whether to insert line breaks every 76 characters in the output.
     * @throws IOException if there is a problem reading or writing.
     */
    static void encode(InputStream in, OutputStream out, boolean lineBreaks) throws IOException {
        // Base64 encoding converts three bytes of input to
        // four bytes of output
		int[] inBuffer = new int[3];
        int lineCount = 0;

		boolean done = false;
        while (!done && (inBuffer[0] = in.read()) != -1){
            // Fill the buffer
            inBuffer[1] = in.read();
            inBuffer[2] = in.read();

            // Calculate the outBuffer
            // The first byte of our in buffer will always be valid
            // but we must check to make sure the other two bytes
            // are not -1 before using them.
            // The basic idea is that the three bytes get split into
            // four bytes along these lines:
            //      [AAAAAABB] [BBBBCCCC] [CCDDDDDD]
            // [xxAAAAAA] [xxBBBBBB] [xxCCCCCC] [xxDDDDDD]
            // bytes are considered to be zero when absent.
            // the four bytes are then mapped to common ASCII symbols

            // A's: first six bits of first byte
            out.write(base64Chars[ inBuffer[0] >> 2 ]);
            if (inBuffer[1] != -1){
                // B's: last two bits of first byte, first four bits of second byte
                out.write(base64Chars [(( inBuffer[0] << 4 ) & 0x30) | (inBuffer[1] >> 4) ]);
                if (inBuffer[2] != -1){
                    // C's: last four bits of second byte, first two bits of third byte
                    out.write(base64Chars [((inBuffer[1] << 2) & 0x3c) | (inBuffer[2] >> 6) ]);
                    // D's: last six bits of third byte
                    out.write(base64Chars [inBuffer[2] & 0x3F]);
                } else {
                    // C's: last four bits of second byte
					out.write(base64Chars [((inBuffer[1] << 2) & 0x3c)]);
                    // an equals sign for a character that is not a Base64 character
                    out.write('=');
                    done = true;
                }
            } else {
                // B's: last two bits of first byte
                out.write(base64Chars [(( inBuffer[0] << 4 ) & 0x30)]);
                // an equal signs for characters that is not a Base64 characters
                out.write('=');
                out.write('=');
                done = true;
            }
            lineCount += 4;
            if (lineBreaks && lineCount >= 76){
                out.write('\n');
                lineCount = 0;
            }
        }
    }

    /**
     * Decode a Base64 encoded String.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
	 * The String is converted to and from bytes according to the platform's
     * default character encoding.
     *
     * @param string The data to decode.
     * @return A decoded String.
     */
     static String decode(String string){
        return new String(decode(string.getBytes()));
    }

    /**
     * Decode a Base64 encoded String.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     *
     * @param string The data to decode.
     * @param enc Character encoding to use when converting to and from bytes.
     * @throws UnsupportedEncodingException if the character encoding specified is not supported.
     * @return A decoded String.
     */
     static String decode(String string, String enc) throws UnsupportedEncodingException {
        return new String(decode(string.getBytes(enc)), enc);
    }

    /**
     * Decode Base64 encoded bytes.
     * Characters that are not part of the Base64 alphabet are ignored
     * in the input.
     *
     * @param bytes The data to decode.
     * @return Decoded bytes.
     */
     public static byte[] decode(byte[] bytes){
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        // calculate the length of the resulting output.
        // in general it will be at most 3/4 the size of the input
        // but the input length must be divisible by four.
        // If it isn't the next largest size that is divisible
        // by four is used.
        int mod;
        int length = bytes.length;
        if ((mod = length % 4) != 0){
            length += 4 - mod;
        }
        length = length * 3 / 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(length);
        try {
        	decode(in, out, false);
        } catch (IOException x){
            // This can't happen.
            // The input and output streams were constructed
            // on memory structures that don't actually use IO.
        }
        return out.toByteArray();
    }

    /**
     * Reads the next (decoded) Base64 character from the input stream.
     * Non Base64 characters are skipped.
     *
     * @param in Stream from which bytes are read.
     * @param throwExceptions Throw an exception if an unexpected character
     *    is encountered.
     * @return the next Base64 character from the stream or -1 if
     *    there are no more Base64 characters on the stream.
     * @throws IOException if an IO Error occurs or if an unexpected character
     *    is encountered.
     */
    private static final int readBase64(InputStream in, boolean throwExceptions) throws IOException {
        int read;
        do {
            read = in.read();
            if (read == -1) return -1;
            if (throwExceptions && reverseBase64Chars[(byte)read] == -1 && 
				read != ' ' && read != '\n'  && read != '\r' && read != '\t' && read != '\f' && read != '='){
                throw new IOException ("Unexpected Base64 character: " + read);
			}
            read = reverseBase64Chars[(byte)read];
        } while (read == -1);
        return read;
    }

    /**
     * Decode Base64 encoded data from the InputStream to the OutputStream.
     * Characters in the Base64 alphabet, white space and equals sign are 
	 * expected to be in urlencoded data.  The presence of other characters
     * could be a sign that the data is corrupted.
     *
     * @param in Stream from which to read data that needs to be decoded.
     * @param out Stream to which to write decoded data.
     * @param throwExceptions Whether to throw exceptions when unexpected data is encountered.
     * @throws IOException if an IO occurs or unexpected data is encountered.
     */
     static void decode(InputStream in, OutputStream out, boolean throwExceptions) throws IOException {
        // Base64 decoding converts four bytes of input to three bytes of output
		int[] inBuffer = new int[4];

        // read bytes unmapping them from their ASCII encoding in the process
        // we must read at least two bytes to be able to output anything
        boolean done = false;
        while (!done && (inBuffer[0] = readBase64(in, throwExceptions)) != -1
			&& (inBuffer[1] = readBase64(in, throwExceptions)) != -1){
            // Fill the buffer
            inBuffer[2] = readBase64(in, throwExceptions);
            inBuffer[3] = readBase64(in, throwExceptions);

            // Calculate the output
            // The first two bytes of our in buffer will always be valid
            // but we must check to make sure the other two bytes
            // are not -1 before using them.
            // The basic idea is that the four bytes will get reconstituted
            // into three bytes along these lines:
            // [xxAAAAAA] [xxBBBBBB] [xxCCCCCC] [xxDDDDDD]
            //      [AAAAAABB] [BBBBCCCC] [CCDDDDDD]
            // bytes are considered to be zero when absent.

            // six A and two B
            out.write(inBuffer[0] << 2 | inBuffer[1] >> 4);
            if (inBuffer[2] != -1){
                // four B and four C
                out.write(inBuffer[1] << 4 | inBuffer[2] >> 2);
                if (inBuffer[3] != -1){
                    // two C and six D
                    out.write(inBuffer[2] << 6 | inBuffer[3]);
                } else {
                    done = true;
                }
            } else {
                done = true;
            }
        }
    }
}
