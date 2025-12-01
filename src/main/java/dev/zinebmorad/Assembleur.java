package dev.zinebmorad;

//importer les bilbiotheques
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Assembleur {
    //create hasmap that combines assmebly code with opcode
    private static final Map<String, Integer> instructionSet = new HashMap<>();

    static {
        // ============================================
        // 8-BIT LOAD & STORE (A & B)
        // ============================================
        // LDA
        instructionSet.put("LDA_IMM", 0x86);
        instructionSet.put("LDA_DIR", 0x96);
        instructionSet.put("LDA_EXT", 0xB6);
        // LDB
        instructionSet.put("LDB_IMM", 0xC6);
        instructionSet.put("LDB_DIR", 0xD6);
        instructionSet.put("LDB_EXT", 0xF6);
        // STA
        instructionSet.put("STA_DIR", 0x97);
        instructionSet.put("STA_EXT", 0xB7);
        // STB
        instructionSet.put("STB_DIR", 0xD7);
        instructionSet.put("STB_EXT", 0xF7);

        // ============================================
        // 16-BIT LOAD & STORE (X, Y, U, S, D)
        // ============================================
        // LDD
        instructionSet.put("LDD_IMM", 0xCC);
        instructionSet.put("LDD_DIR", 0xDC);
        instructionSet.put("LDD_EXT", 0xFC);
        // LDX
        instructionSet.put("LDX_IMM", 0x8E);
        instructionSet.put("LDX_DIR", 0x9E);
        instructionSet.put("LDX_EXT", 0xBE);
        // LDY (Page 2 opcodes not supported by simple assembler yet, strictly 1-byte opcodes here)
        // Note: Java map usually stores Integer. If you have 0x108E, handle it carefully.
        // For simplicity, we stick to standard map. Your CPU handles 0x10 prefix logic inside step().
        // If you want to assemble 0x108E, simply put 0x108E in the map!

        instructionSet.put("LDY_IMM", 0x108E);
        instructionSet.put("LDY_DIR", 0x109E);
        instructionSet.put("LDY_EXT", 0x10BE);

        instructionSet.put("LDU_IMM", 0xCE);
        instructionSet.put("LDU_DIR", 0xDE);
        instructionSet.put("LDU_EXT", 0xFE);

        instructionSet.put("LDS_IMM", 0x10CE);
        instructionSet.put("LDS_DIR", 0x10DE);
        instructionSet.put("LDS_EXT", 0x10FE);

        // STORE 16-bit
        instructionSet.put("STD_DIR", 0xDD);
        instructionSet.put("STD_EXT", 0xFD);
        instructionSet.put("STX_DIR", 0x9F);
        instructionSet.put("STX_EXT", 0xBF);
        instructionSet.put("STY_DIR", 0x109F);
        instructionSet.put("STY_EXT", 0x10BF);
        instructionSet.put("STU_DIR", 0xDF);
        instructionSet.put("STU_EXT", 0xFF);
        instructionSet.put("STS_DIR", 0x10DF);
        instructionSet.put("STS_EXT", 0x10FF);

        // ============================================
        // ARITHMETIC & LOGIC (ALU)
        // ============================================
        // ADDA / ADDB
        instructionSet.put("ADDA_IMM", 0x8B);
        instructionSet.put("ADDA_DIR", 0x9B);
        instructionSet.put("ADDA_EXT", 0xBB);
        instructionSet.put("ADDB_IMM", 0xCB);
        instructionSet.put("ADDB_DIR", 0xDB);
        instructionSet.put("ADDB_EXT", 0xFB);
        // ADDD (16-bit add)
        instructionSet.put("ADDD_IMM", 0xC3);
        instructionSet.put("ADDD_DIR", 0xD3);
        instructionSet.put("ADDD_EXT", 0xF3);
        // SUBA / SUBB
        instructionSet.put("SUBA_IMM", 0x80);
        instructionSet.put("SUBA_DIR", 0x90);
        instructionSet.put("SUBA_EXT", 0xB0);
        instructionSet.put("SUBB_IMM", 0xC0);
        instructionSet.put("SUBB_DIR", 0xD0);
        instructionSet.put("SUBB_EXT", 0xF0);
        // SUBD (16-bit sub)
        instructionSet.put("SUBD_IMM", 0x83);
        instructionSet.put("SUBD_DIR", 0x93);
        instructionSet.put("SUBD_EXT", 0xB3);

        // AND
        instructionSet.put("ANDA_IMM", 0x84);
        instructionSet.put("ANDA_DIR", 0x94);
        instructionSet.put("ANDA_EXT", 0xB4);
        instructionSet.put("ANDB_IMM", 0xC4);
        instructionSet.put("ANDB_DIR", 0xD4);
        instructionSet.put("ANDB_EXT", 0xF4);
        // ORA / ORB
        instructionSet.put("ORA_IMM", 0x8A);
        instructionSet.put("ORA_DIR", 0x9A);
        instructionSet.put("ORA_EXT", 0xBA);
        instructionSet.put("ORB_IMM", 0xCA);
        instructionSet.put("ORB_DIR", 0xDA);
        instructionSet.put("ORB_EXT", 0xFA);
        // EORA / EORB
        instructionSet.put("EORA_IMM", 0x88);
        instructionSet.put("EORA_DIR", 0x98);
        instructionSet.put("EORA_EXT", 0xB8);
        instructionSet.put("EORB_IMM", 0xC8);
        instructionSet.put("EORB_DIR", 0xD8);
        instructionSet.put("EORB_EXT", 0xF8);

        // COMPARE (CMP)
        instructionSet.put("CMPA_IMM", 0x81);
        instructionSet.put("CMPA_DIR", 0x91);
        instructionSet.put("CMPA_EXT", 0xB1);
        instructionSet.put("CMPB_IMM", 0xC1);
        instructionSet.put("CMPB_DIR", 0xD1);
        instructionSet.put("CMPB_EXT", 0xF1);
        instructionSet.put("CMPX_IMM", 0x8C);
        instructionSet.put("CMPX_DIR", 0x9C);
        instructionSet.put("CMPX_EXT", 0xBC);

        // ============================================
        // INHERENT (Single Byte Instructions)
        // ============================================
        instructionSet.put("INCA_INH", 0x4C);
        instructionSet.put("INCB_INH", 0x5C);
        instructionSet.put("DECA_INH", 0x4A);
        instructionSet.put("DECB_INH", 0x5A);
        instructionSet.put("CLRA_INH", 0x4F);
        instructionSet.put("CLRB_INH", 0x5F);
        instructionSet.put("TSTA_INH", 0x4D);
        instructionSet.put("TSTB_INH", 0x5D);
        instructionSet.put("COMA_INH", 0x43);
        instructionSet.put("COMB_INH", 0x53);
        instructionSet.put("NEGA_INH", 0x40);
        instructionSet.put("NEGB_INH", 0x50);
        instructionSet.put("NOP_INH", 0x12);
        instructionSet.put("RTS_INH", 0x39);
        instructionSet.put("RTI_INH", 0x3B);
        instructionSet.put("SWI_INH", 0x3F);

        // SHIFTS
        instructionSet.put("ASLA_INH", 0x48);
        instructionSet.put("ASLB_INH", 0x58);
        instructionSet.put("LSRA_INH", 0x44);
        instructionSet.put("LSRB_INH", 0x54);
        instructionSet.put("ROLA_INH", 0x49);
        instructionSet.put("ROLB_INH", 0x59);
        instructionSet.put("RORA_INH", 0x46);
        instructionSet.put("RORB_INH", 0x56);

        // ============================================
        // BRANCHES (Relative)
        // ============================================
        instructionSet.put("BRA_REL", 0x20);
        instructionSet.put("BNE_REL", 0x26);
        instructionSet.put("BEQ_REL", 0x27);
        instructionSet.put("BCC_REL", 0x24);
        instructionSet.put("BCS_REL", 0x25);
        instructionSet.put("BMI_REL", 0x2B);
        instructionSet.put("BPL_REL", 0x2A);
        instructionSet.put("BGT_REL", 0x2E);
        instructionSet.put("BLT_REL", 0x2D);
        instructionSet.put("BGE_REL", 0x2C);
        instructionSet.put("BLE_REL", 0x2F);
        instructionSet.put("BSR_REL", 0x8D);

        // EXTENDED JUMPS
        instructionSet.put("JMP_EXT", 0x7E);
        instructionSet.put("JSR_EXT", 0xBD);
    }

    //read the instruction
    private static void addInstruction(List<Integer> code, String key, int val, int byteCount) throws Exception {
        if (!instructionSet.containsKey(key)) throw new Exception("Invalid Opcode/Mode: " + key);

        code.add(instructionSet.get(key)); // Add Opcode

        if (byteCount == 2) {
            code.add((val >> 8) & 0xFF); // High Byte
            code.add(val & 0xFF);        // Low Byte
        } else {
            code.add(val & 0xFF);        // Single Byte
        }
    }
    // convert assmebly code to hex code

    public static List<Integer> assemble(String text) throws Exception {
        List<Integer> byteCode = new ArrayList<>();
        // split text to lines
        String[] lines = text.toUpperCase().split("\\n");

        for (String line : lines){
            line = line.trim();
            if(line.isEmpty() || line.startsWith(";")) continue; // on skip les emptys lines

            String[] parts = line.split("\\s+");
            String mnemonic = parts[0];

            if (parts.length == 1){
                //inherit
                String key = mnemonic + "_INH";
                if (!instructionSet.containsKey(key)) throw new Exception("Unknown Instruction: "+line);
                byteCode.add(instructionSet.get(key));
        } else {
                String operandStr = parts[1];
                String mode = "";
                int value = 0;
                if (operandStr.startsWith("#$")) {
                    //immediate
                    mode ="IMM";
                    value = Integer.parseInt(operandStr.substring(2),16);
                    String key = mnemonic + "_" + mode;
                    addInstruction(byteCode, key, value ,1);
                } else if (operandStr.startsWith("$")){
                    // direct or extend
                    value = Integer.parseInt(operandStr.substring(1),16);
                    if (value <= 255) {
                        if (instructionSet.containsKey(mnemonic +"_DIR")){
                            mode = "DIR";//directe
                            addInstruction(byteCode,mnemonic +"_"+mode,value,1);
                        } else {
                            mode = "EXT";//extend
                            addInstruction(byteCode,mnemonic+"_"+mode,value,2);
                        }
                    } else {
                        mode ="REL";
                        value = Integer.parseInt(operandStr,16);
                        String key = mnemonic + "_" + mode;
                        addInstruction(byteCode,key,value,1);
                    }
                }
            }
    } return byteCode ;
}

}
