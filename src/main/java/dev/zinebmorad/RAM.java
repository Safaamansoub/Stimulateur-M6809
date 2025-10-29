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


    //fro writing
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

}
