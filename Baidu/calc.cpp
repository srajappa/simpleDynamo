public class Solution {
    public int integerBreak(int n) {
        if( n==0)
            return 0;
        int[] arr = new int[n+1];
        arr[0] = 0;
        arr[1] = 1;

        return compute(n,arr);
    }
    public int compute(int num, int[] arr){
        if(num==1)
            return 1;
        int max = Integer.MIN_VALUE;

        for(int j=num-1; j >=num/2 ; j--){
            if(arr[j]!=0){
                max = Math.max(max, arr[j]);
            }else{
                max = Math.max(compute(arr[j],arr),max);    //Return the best value
            }
        }
        arr[num] = max;
    }
}


#ifndef __main__

#define __main__



int main(int argc, char* argv[]) {

  Calculator calculator;

  cout << "First Step" << endl;

  // should print 5.85714

  cout << calculator.Calc("3 + 4 * 5 / 7") << endl;



  cout << "\nSecond Step" << endl;

  // should print a 5

  cout << calculator.Calc("( 10 + 4 ) * 5 / 7") << endl;



  cout << "\nThird Step" << endl;

  // should print 3 and 243. 3 for "pi" and 243 for "9 * 9 * 3".

  vector<double> output = calculator.CalcWithVars(

      { "pi = 3", "pizza = 9 * 9 * pi" });

  for (size_t i = 0; i < output.size(); ++i) {

    cout << output[i] << endl;

  }

}



#endif
