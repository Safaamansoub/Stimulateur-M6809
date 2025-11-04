package dev.zinebmorad;

public class RAM {
    //table for stockage
    private byte[] memory;
    private static final int memory_size = 65536 ;
    //constructor
    public RAM(){
        this.memory = new byte[memory_size] ;
    }


    //methodes
    //for reading
    public int ReadFromMemory(int address){
        //test for the validation of the adress
        if (address <0 || address >= memory_size ){
            throw new IndexOutOfBoundsException("Adress memory invalid : " + address) ;
        }
        return this.memory[address] & 0xFF ;
    }


    //for writing
    public void WriteInMemory(int address, int value){
        //test for the validation of the adress
        if (address <0 || address >= memory_size ){
            throw new IndexOutOfBoundsException("Adress memory invalid : " + address) ;
        }
        //verification of the value
        if(value < 0 || value >255) {
            throw new IllegalArgumentException("Value should be on (0-255) : "+ value);
        }
        //entering the value as a information
        this.memory[address] =(byte) value ;

    }

    // new helper functions
    public  int ReadWord(int adress){
        // read the hight byte
        int hightByte = ReadFromMemory(adress) ;
        // read the low byte from the next adress
        int lowByte = ReadFromMemory(adress + 1 ) ;

        // combiner les 2
        return (hightByte << 8 ) | lowByte ;
    }

    public  void WriteWord(int adress , int wordValue){
        //njibo the hight byte
        int hightByte = (wordValue >> 8)& 0xFF ;
        //njibo low byte
        int lowByte = wordValue & 0xFF ;

        // db nktbohoome f memoire
        WriteInMemory(adress, hightByte);
        WriteInMemory(adress +1 ,lowByte);
    }

}
