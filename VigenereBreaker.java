/*
 * To change this license header, choose License Headers in Project Properties.
 //12345
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vigenerecipher;

/**
 *
 * @author Ross Cournoyer rcoupvc@yahoo.com
 */

import edu.duke.*;
import java.io.File;
import java.util.*;

public class VigenereBreaker {
    
    //read the words from a dictionary into a HashSet and return it
    public HashSet<String> readDictionary(FileResource dictionary) {
        HashSet<String> dict = new HashSet<>();
        
        for (String word : dictionary.lines()) {
            if (!dict.contains(word)) {
                dict.add(word.toLowerCase());
            }
        }
        
        return dict;
    }
    
    //find the most common character in a dictionary of words
    public char mostCommonCharIn(HashSet<String> dictionary) {
        HashMap<Character,Integer> charCount = new HashMap<>();
        char mostCommon = 'g';
        int highestCount = 0;
        
        for (String s : dictionary) {
            for (int i=0; i < s.length(); ++i) {
                if (!charCount.containsKey(s.charAt(i))) {
                    charCount.put(s.charAt(i), 1);
                }
                else {
                    charCount.put(s.charAt(i),charCount.get(s.charAt(i)) + 1);
                }   
            }  
        }
        for (Character c : charCount.keySet()) {
            if (charCount.get(c) > highestCount) {
                highestCount = charCount.get(c);
                mostCommon = c;
            }
        }
        return mostCommon;
    }
    
    //count and return the number of "real words" in the dictionary
    public int wordCount(String message, HashSet dictionary) {
        int realWords = 0;
        String[] words = message.split("\\W+");
        
        for (int i=0; i < words.length; ++i) {
            if (dictionary.contains(words[i].toLowerCase())) {
                ++realWords;
            }
        }
        
        return realWords;
    }
    
    public void breakForAllLangs(String encrypted, HashMap<String,HashSet<String>> langs) {
        int bestWordCount = 0;
        String bestDecrypt = null;
        String detectedLanguage = null;
        for (String s : langs.keySet()) {
            String decrypted = breakForLanguage(encrypted, langs.get(s));
            int currWordCount = wordCount(decrypted, langs.get(s));
            if (currWordCount > bestWordCount) {
                bestWordCount = currWordCount;
                bestDecrypt = decrypted;
                detectedLanguage = s;
            }
        }
        System.out.println("Number of correct words detected is: " + bestWordCount);
        System.out.println("Detected Language is: " + detectedLanguage);
        System.out.println(bestDecrypt);
    }
    
    //given a language, find the best key for that language
    //best key based on counting the highest number of "real words"
    public String breakForLanguage (String encrypted, HashSet dictionary) {
        int bestWords = 0;
        int keyLength = 0;
        char mostCommon = mostCommonCharIn(dictionary);
        
        for (int i=1; i <= 100; ++i) {
            int[] key = tryKeyLength(encrypted,i, mostCommon);
            VigenereCipher vc = new VigenereCipher(key);
            int currWords = wordCount(vc.decrypt(encrypted),dictionary);
            if (currWords > bestWords) {
                bestWords = currWords;
                keyLength = i; 
            }
            
        }
        //System.out.println(keyLength);
        //System.out.println(bestWords);
        int[] bestKey = tryKeyLength(encrypted, keyLength, mostCommon);
        VigenereCipher vc = new VigenereCipher(bestKey);
        String result = vc.decrypt(encrypted);
        //System.out.println(wordCount(result, dictionary));
        return result;
    }
    
    //
    public String sliceString(String message, int whichSlice, int totalSlices) {
        StringBuilder slice = new StringBuilder();
        
        for (int i=whichSlice; i < message.length(); i+=totalSlices) {
            slice.append(message.charAt(i));
        }
        message = slice.toString();
        return message;
    }

    public int[] tryKeyLength(String encrypted, int klength, char mostCommon) {
        int[] key = new int[klength];
        String[] slices = new String[klength];
        int k = 0;
        while (k < klength) {
            slices[k] = sliceString(encrypted, k, klength);
            ++k;
        }
        CaesarCracker crack = new CaesarCracker();
        for (int i=0; i < klength; ++i) {
            key[i] = crack.getKey(slices[i]);
        }
        return key;
    }

    public void breakVigenere () {
        FileResource encrypted = new FileResource("secretmessage3.txt");
        String input = encrypted.asString();
        HashMap<String, HashSet<String>> dics = new HashMap<>();
        DirectoryResource d = new DirectoryResource(); //ask for dictionaries
        for (File f : d.selectedFiles()) {
            FileResource dic = new FileResource(f);
            System.out.println("Loading " + f.getName() + " dictionary.");
            dics.put(f.getName(),readDictionary(dic));
        }
        breakForAllLangs(input,dics);
    }
    
}

//La chambre à coucher de Juliette.
//Drei Hexen treten auf.
