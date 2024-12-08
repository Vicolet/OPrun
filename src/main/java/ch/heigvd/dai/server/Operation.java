package ch.heigvd.dai.server;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Operation {
    private static final int MAX_NUMBER = 10;
    private static final int MAX_NUMBER_BOUND = 50;
    private static final int MAX_MULT_COEFF = 15;

    private final String operation;
    private int[] numbers;
    private char[] operators;

    /**
     * Create an operations with random nbNumbers numbers and random nbNumbers-1 operators
     * @param nbNumbers nbNumbers in the operation
     */
    public Operation(int nbNumbers){
        if(nbNumbers > MAX_NUMBER) nbNumbers = MAX_NUMBER;
        operation = createOperation(getRandomOperators(nbNumbers - 1));
    }

    /**
     * Create an operations with given numbers and operators
     * @param numbers array of numbers
     * @param operators array of operators
     */
    public Operation(int[] numbers, char[] operators){
        //Checks
        if(numbers.length != operators.length + 1){
            //Need to be n numbers and n-1 operators
            throw new RuntimeException();
        }
        for(int i = 0; i < numbers.length; i++){
            if(numbers[i] > MAX_NUMBER_BOUND || numbers[i] <= 0){
                throw new RuntimeException();
            }
            if(i < numbers.length - 1){
                if(!(operators[i] == '+' || operators[i] == '-' || operators[i] == '*')){
                    throw new RuntimeException(); //operators contains other illegal operators
                }
            }
        }

        this.numbers = numbers;
        this.operators = operators;

        StringBuilder result = new StringBuilder();
        for(int i = 0; i < numbers.length; i++){
            result.append(numbers[i]);
            if(i < numbers.length - 1)
                result.append(operators[i]);
        }
        operation = result.toString();
    }

    /**
     * Creates the array of random operators
     * @param nbOperators random operators
     * @return the array of operators
     */
    private char[] getRandomOperators(int nbOperators){

        Random rand = new Random();
        char[] operators = new char[nbOperators];
        int rnd;
        boolean gotMult = false;
        //Only one multiplication allowed in an operation
        for(int i = 0; i < nbOperators; i++){
            if(gotMult){
                rnd = rand.nextInt(2);
            }else{
                rnd = rand.nextInt(3);
            }
            switch (rnd){
                case 0: operators[i] = '+'; break;
                case 1: operators[i] = '-'; break;
                case 2:
                    operators[i] = '*';
                    gotMult = true;
                break;
            }
        }
        return operators;
    }

    /**
     *
     * @param operators array of operators to be added
     * @return the operation created as a string
     */
    private String createOperation(char[] operators){
        int nbNumbers = operators.length + 1;
        int[] numbers = new int[nbNumbers];
        Random rand = new Random();

        //  + - * + * //5 operators
        // 6 5 4 3 2 1 //6 numbers

        //Create numbers
        for(int i = 0; i < nbNumbers; i++){
            numbers[i] = rand.nextInt(1, MAX_NUMBER_BOUND); // 1 to MAX_NUMBER_BOUND
        }

        //multiplication operator never multiplies numbers higher than MAX_MULT_COEFF
        for(int i = 0; i < operators.length; i++){
            if(operators[i] == '*'){
                numbers[i] = rand.nextInt(1, MAX_MULT_COEFF);
                numbers[i+1] = rand.nextInt(1, MAX_MULT_COEFF);
            }
        }

        this.numbers = numbers;
        this.operators = operators;

        //Create the result string
        StringBuilder result = new StringBuilder();
        for(int i = 0; i < nbNumbers; i++){
            result.append(numbers[i]);
            if(i < nbNumbers - 1)
                result.append(operators[i]);
        }

        return result.toString();
    }

    /**
     *
     * @return The result of the operation
     */
    public int getResult(){
        if(operation == null) return 0;

        //Create stacks
        ArrayList<Integer> num = new ArrayList<Integer>();
        ArrayList<Character> op = new ArrayList<Character>();
        for(int i = 0; i < numbers.length; i++){
            num.add(numbers[i]);
            if(i < numbers.length - 1)
                op.add(operators[i]);
        }

        //Multiplication first and deletes the * operator from the stack
        for(int i = 0; i < op.size(); i++){
            if(op.get(i) == '*'){
                num.set(i, num.get(i) * num.get(i+1));
                num.remove(i+1);
                op.remove(i);
            }
        }

        //Adds and subtracts the remaining values
        int result = num.getFirst();
        for(int i = 0; i < op.size(); i++){
            switch(op.get(i)){
                case '+':
                    result += num.get(i+1);
                    break;
                case '-':
                    result -= num.get(i+1);
                    break;
            }
        }

        return result;
    }

    /**
     *
     * @return The operation as a string
     */
    public String toString(){
        return Objects.requireNonNullElse(operation, "");
    }

    public static void main(String[] args){
        Operation op = new Operation(4);
        System.out.print(op);
        System.out.print(" = " + op.getResult());
    }
}
