#include <iostream>

#include <map>

#include <sstream>

#include <string>

#include <vector>

#include <stdlib.h>

#include <stack>

using namespace std;



int HasHigherPrecedence(char op1, char op2);

vector<char> InfixToPostfix(string expression);

bool IsOperand(char C);

int GetOperatorWeight(char op);

bool IsOperator(char C);

int IsRightAssociative(char op);



bool is_number(const string& s) {

    string::const_iterator it = s.begin();

    if (*it == '-') {

        ++it;

    }

    if (it == s.end()) {

        return false;

    }

    while (it != s.end() && (isdigit(*it) || (*it == '.'))) {

        ++it;

    }

    return !s.empty() && it == s.end();

}



double to_number(const string& s) {

    return atof(s.c_str());

}



vector<string> split(const string &s, char delim) {

    vector<string> elems;

    stringstream ss(s);

    string item;

    while (getline(ss, item, delim)) {

        elems.push_back(item);

    }

    return elems;

}



vector<char> InfixToPostfix(string expression)

{

// Declaring a Stack from Standard template library in C++.

stack<char> S;

string postfix = ""; // Initialize postfix as empty string.

    vector<char> v;

for(int i = 0;i< expression.length();i++) {

        

// Scanning each character from left.

// If character is a delimitter, move on.

if(expression[i] == ' ' || expression[i] == ',') continue;

        

// If character is operator, pop two elements from stack, perform operation and push the result back.

else if(IsOperator(expression[i]))

{

while(!S.empty() && S.top() != '(' && HasHigherPrecedence(S.top(),expression[i]))

{

postfix+= ' ' + S.top();

                v.push_back(S.top());

S.pop();

}

S.push(expression[i]);

}

// Else if character is an operand

else if(IsOperand(expression[i]))

{

postfix += ' ' + expression[i];

            v.push_back(expression[i]);

}

        

else if (expression[i] == '(')

{

S.push(expression[i]);

}

        

else if(expression[i] == ')')

{

while(!S.empty() && S.top() !=  '(') {

postfix += ' ' + S.top();

                v.push_back(S.top());

S.pop();

}

S.pop();

}

}

    

while(!S.empty()) {

postfix += ' ' + S.top();

        v.push_back(S.top());

S.pop();

}

    

return v;

}



// Function to verify whether a character is english letter or numeric digit.

// We are assuming in this solution that operand will be a single character

bool IsOperand(char C)

{

if(C >= '0' && C <= '9') return true;

if(C >= 'a' && C <= 'z') return true;

if(C >= 'A' && C <= 'Z') return true;

return false;

}



// Function to verify whether a character is operator symbol or not.

bool IsOperator(char C)

{

if(C == '+' || C == '-' || C == '*' || C == '/' || C== '$')

return true;

    

return false;

}



// Function to verify whether an operator is right associative or not.

int IsRightAssociative(char op)

{

if(op == '$') return true;

return false;

}



// Function to get weight of an operator. An operator with higher weight will have higher precedence.

int GetOperatorWeight(char op)

{

int weight = -1;

    

    if(op == '+' or op == '-')return 1;

    if(op == '*' or op == '/')return 2;

    if(op == '$')return 3;

}



// Function to perform an operation and return output.

int HasHigherPrecedence(char op1, char op2)

{

int op1Weight = GetOperatorWeight(op1);

int op2Weight = GetOperatorWeight(op2);

    

// If operators have equal precedence, return true if they are left associative.

// return false, if right associative.

// if operator is left-associative, left one should be given priority.

if(op1Weight == op2Weight)

{

if(IsRightAssociative(op1)) return false;

else return true;

}

return op1Weight > op2Weight ?  true: false;

}



class Calculator {

 public:

    

  double Calc(const string& input) const {

    // IMPLEMENT ME

      double result = 0.0;

    vector<char> res1 = InfixToPostfix(input);

      

      vector<string> res;

      for(int i=0;i<(int)res1.size();i++)

      {

          string s1;

          s1 += res1[i];

          res.push_back(s1);

      }

      stack<string> s;

      for(int i=0;i<(int)res.size();i++) {

          // cout << res[i] << "\n";

          

          if(res[i] == "*") {

              string c1 = s.top();s.pop();

              string c2 = s.top();s.pop();

              result = stod(c1)*stod(c2);

              s.push(to_string(result));

          }

          else if(res[i] == "/") {

              string c1 = s.top();s.pop();

              string c2 = s.top();s.pop();

              // cout << stod(c2) << " " << stod(c1) << "\n";

              result = stod(c2)/stod(c1);

              s.push(to_string(result));

          }

          else if(res[i] == "+") {

              string c1 = s.top();s.pop();

              string c2 = s.top();s.pop();

              result = stod(c1) + stod(c2);

              s.push(to_string(result));

          }

          else if(res[i] == "-") {

              string c1 = s.top();s.pop();

              string c2 = s.top();s.pop();

              result = stod(c2) - stod(c1);

              s.push(to_string(result));



          }

        else s.push(res[i]);

        // cout << result << " ";

      }



    return result;

  }



  vector<double> CalcWithVars(const vector<string>& inputs) const {

    // IMPLEMENT ME

    vector<double> output;

    return output;

  }

};





#ifndef __main__

#define __main__



int main(int argc, char* argv[]) {

  Calculator calculator;

  cout << "First Step" << endl;

  // should print 5.85714

  cout << calculator.Calc("3 + 4 * 5 / 7") << endl;



  cout << "\nSecond Step" << endl;

  // should print a 5

  cout << calculator.Calc("( 3 + 4 ) * 5 / 7") << endl;



  cout << "\nThird Step" << endl;

  // should print 3 and 243. 3 for "pi" and 243 for "9 * 9 * 3".

  vector<double> output = calculator.CalcWithVars(

      { "pi = 3", "pizza = 9 * 9 * pi" });

  for (size_t i = 0; i < output.size(); ++i) {

    cout << output[i] << endl;

  }

}

