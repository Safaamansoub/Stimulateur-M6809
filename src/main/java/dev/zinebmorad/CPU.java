package dev.zinebmorad;

public class CPU {

    // ETAPE 1 : DECLARATION PART

    // we need a reference to the memory
    private RAM ram;


    // declaration of the motorola's REGISTERS :
    // Accumulators
    private int A;
    private int B;
    // private int D ; im not gonna declared it cause its gonna be like a couple of A and B
    // Index Registres
    private int X;
    private int Y;
    //Pointer Registres
    private int U; //user stack pointer
    private int S; //System stack pointer
    //Program Counter
    private int PC;
    //Direct page pointer
    public int DP ;

    //Condition code register // his job is to hold 8 flags (status bits)
    private int CC ;

    // declaration of the flags :
    public  static final int C_FLAG = 0x01 ;
    public  static final int V_FLAG = 0x02 ;
    public  static final int Z_FLAG = 0x04 ;
    public  static final int N_FLAG = 0x08 ;
    public  static final int I_FLAG = 0x10 ;
    public  static final int H_FLAG = 0x20 ;
    public  static final int F_FLAG = 0x40 ;
    public  static final int E_FLAG = 0x80 ;

    // ETAPE 2 : THE FUNCTIONS HELPERS :
    //  Fct for reseting all the values of the registers to 0
    public void reset (){
        A = 0 ;
        B = 0 ;
        X = 0 ;
        Y = 0 ;
        U = 0 ;
        S = 0 ;
        DP = 0 ;
        CC = 0 ;

        // For the counter
        // this needs to be with the ram but first we need the constructor for the ram created there
        PC = ram.ReadWord(0xFFFE);
        //for the 16-bit adress goes from FFFE ( HIGHT BYTE) and FFFF (low byte )
    }

    // constructor :
    public CPU(RAM ram ){
        this.ram = ram ;
        reset();//once we create we reset
    }

    // functions for print all the values of all the registres
    public int getA(){
        return A;
    }
    public int getB(){
        return B;
    }
    public int getX(){
        return X;
    }
    public int getY(){
        return Y;
    }
    public int getU(){
        return U;
    }
    public int getS(){
        return S;
    }
    public int getDP(){
        return DP ;
    }
    public int getCC() {return CC;}
    public int getPC(){
        return PC;
    }

    // functions for flags
    // we have the zero and the negatives ones are used by 80% mn les instrcutions and the others are like udpates by some special ones
    //we gonna create 3 functions that geos with any flags then functions that gonna see those most commen ones
    private void setFlag(int flag){
        CC = CC | flag ;
    }
    private  void clearFlag (int flag ){
        CC &= ~flag ;
    }
    private boolean isFlagSet( int flag){
        return (CC & flag) != 0;
    }

    // functions for the adressing modes
    // we have 6 adressing mode
    // for the inherent mode we need no function the opcode is enough
    // for the immediate mode "the data is the very next byte or word after the opcode " we
    /* for this we need to call the function fetch */
    // we add the function fetchByte and ftechWord
    public int fetchByte(){
        int value = ram.ReadFromMemory(PC);
        PC = (PC+1) & 0xFFFF ;
        return value ;
    }
    public int fetchWord(){
        int value = ram.ReadWord(PC);
        PC = (PC + 2) & 0xFFFF;
        return value ;
    }
    // for the extend mode
    /* the next 2 byte after the opcode from a full 16-bit
    we can just re-use the fetch functions */
    private int getExtendAdress(){
        return  fetchWord() ;
    }
    //Direct mode adressing
    /* the next one byte after the opcode is the law-byte of an adress . the haight
    one comes frim the DP register
    so lk we need the a fucntion that fetch a byte and combine it with the DP
    */
    private int getDirectAdress(){
         int offset = fetchByte();
         return (DP << 8) | offset ;
    }
    // for the others were gonna created them whem we need







}