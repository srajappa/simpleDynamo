#include <vector>
#include <string>
#include <sstream>
#include <iostream>
#include <map>
//#include<boost/tokenizer.hpp>
#include <stack>
#include <deque>
#include <algorithm>
#include <iterator>
#include <cstring>
#include <string.h>
using namespace std;
//using namespace boost;


class Operator{
public:
  enum prec_enum{MINU, ADD, MUL, DIV, BRACK};
  std::string symbol;
  int rank;
  Operator(std::string inputSymbol){
    symbol = inputSymbol;
    if(symbol.compare("*")==0)
      rank = MUL;
    else if(symbol.compare("+")==0)
  		rank = ADD;
  	else if(symbol.compare("-")==0)
  		rank = MINU;
    else if(symbol.compare("/")==0)
      rank = DIV;
  	else
  		rank = BRACK;
  }

  int compare(Operator o2){
    if(rank > o2.rank)
      return 1;
    if(rank < o2.rank)
      return -1;
    return 0;
  }

};


class Calculator{
//private:
  //Map<std::string, double> varStore;
public:

  map<std::string, double> varStore;


  double calculate(std::string expression){
   
    return evaluatePostfix(infixToPostfix(expression));
  }


  string infixToPostfix(std::string expression){
    // vector<std::string> tokens;
    // tokenizer<> tok(expression);
    // for(tokenizer<>::iterator begin = tok.begin(); begin!=tok.end(); begin++){
    //   tokens.push_back(std::string str(begin));
    // }
    //cout<<"_______________________"<<expression << endl;
    istringstream iss(expression);
    vector<string> tokens;
    copy(istream_iterator<string>(iss),
         istream_iterator<string>(),
         back_inserter(tokens));

    // for(int i =0; i < tokens.size(); i++)
    //   cout<<tokens[i] << " ll "<< endl << ;

    //return NULL;
    
    stack<Operator> s;
    std::string result;
    for(int i=0; i<tokens.size(); i++){
      if(s.empty() || (tokens[i].compare("(")==0)){
        Operator x = Operator(tokens[i]); 
        s.push(x);
        continue;
      }

      if(tokens[i].compare(")")==0){
        while(!s.empty() && s.top().symbol.compare("(")!=0){
          result.append(s.top().symbol);
          //cout << s.top().symbol << "aren't you here"<<endl;
          s.pop();
          result.append(" ");
        }
        if(s.empty())
          continue;
        s.pop();
        continue;
      }

      if(tokens[i].find("*+-/") > 0){
        Operator temp = Operator(tokens[i]);
        if(s.top().symbol.compare("(")==0){
          //cout << temp.symbol << " you are here right\n";
          s.push(temp);
          continue;
        }

        if(s.top().compare(temp)==1){
          while(!s.empty() &&  s.top().symbol.compare("(")!=0){
            result.append(s.top().symbol);
            //cout << s.top().symbol << " bug 1"<<endl;
              s.pop();
            result.append(" ");
          }
          if(s.empty()){
            s.push(temp);
            continue;
          }
          s.pop();
          s.push(temp);
        }else{
          s.push(temp);
        }
      }else{
        result.append(tokens[i]);
        //cout << tokens[i] << "bug" <<endl;
        result.append(" ");
      }
    }
    while(!s.empty()){
      result.append(s.top().symbol);
      //cout << s.top().symbol << "bug 1"<<endl;
        s.pop();

      result.append(" ");
    }
    //cout << result << "DB"<<endl;
    return result;
  
  }

  double evaluatePostfix(std::string postfix){
    //cout << postfix <<" // DB evaluatePostfix" <<endl;
    deque<double> opern;
    // vector<std::string> tokens;
    // tokenizer<> tok(expression);
    // for(tokenizer<>::iterator begin = tok.begin(); begin!=tok.end(); begin++){
    //   tokens.push_back(std::string str(begin));
    // }
    istringstream iss(postfix);
    vector<string> tokens;
    copy(istream_iterator<string>(iss),
         istream_iterator<string>(),
         back_inserter(tokens));

    double x;
    double y;

    


    for(int i=0; i<tokens.size(); i++){
      //cout <<tokens[i] <<" //DB check tokens" << endl;
      if(tokens[i].length()==1 && (tokens[i].compare("*")==0 
                               || tokens[i].compare("+")==0
                               || tokens[i].compare("/")==0
                               || tokens[i].compare("-")==0)){
        //cout << tokens[i] << "-> "<< tokens[i].find("*-/+") <<endl;
        y = opern.front();
          opern.pop_front();
        x = opern.front();
          opern.pop_front();


        //cout << x << " and " << y << endl;
        switch(tokens[i].at(0)){
          case '+' : opern.push_front(x+y);
                     break;
          case '*' : opern.push_front(x*y);
                     break;
          case '-' : opern.push_front(x-y);
                     break;
          case '/' : opern.push_front(x/y);
                     break;
        }
      }else{
        //std::string str(tokens[i]);
        //cout << std::stod(str,&sz) << " CHECK result"<<endl;
        std::string::size_type sz;
        ///cout << tokens[i] << endl;
        opern.push_front(std::stod(tokens[i],&sz));
      }
    }
    //cout << opern.front() << endl;
    return opern.front();
  }

  vector<string> split(string str, char delimiter) {
    vector<string> internal;
    stringstream ss(str); // Turn the string into a stream.
    string tok;
  
    while(getline(ss, tok, delimiter)) {
      internal.push_back(tok);
    }
  
    return internal;
  }

  string removeSpace(string str){
    stringstream ss(str); // Turn the string into a stream.
    string tok;
    ////string result;
    while(getline(ss,tok,' ')){
      //
    }
    return tok;
  }

  // vector<double> calculateMulti(vector<string> expressionValues){
  //   vector<string> iVal;
  //   vector<double> result;
  //   string varName,varVal;
  //   double value;
  //   vector<string> indStrings;
  //   string temp;

  //   std::vector<double>::iterator iternat;
  //   iternat = result.begin();
    
  //   for(int i=0; i< expressionValues.size(); i++ ){
  //     iVal = split(expressionValues[i],'=');
  //     //Going into each string within the vector
  //     for(int j=0; j<iVal.size(); j++){
  //       //Assuming the first is always a variable
  //       if(j==0){
  //         //cout << iVal[0] << endl;
  //         varName = infixToPostfix(iVal[j]);
  //       }else if(j==1){

  //         cout<<iVal[1]<<endl;
  //         //Check if the value has a large size or a small size
  //         varVal = infixToPostfix(iVal[j]);
  //         //cout<<varVal.length() << "Size"<<endl;
          
  //         if(varVal.length()==2){
  //           //cout << varVal <<"DE"<< endl;
  //           std::string::size_type sz;
  //           value = std::stod(varVal,&sz);
  //         }else{
  //           cout << varVal << "DE2 "<<endl;
  //           //Infix expression the only other value we can think of
  //           indStrings = split(varVal,' ');
  //           temp="";
  //           for(int k = 0; k < indStrings.size(); k++){
  //             if(varStore.count(indStrings[k])==0 && !varStore.empty()){
  //               std::map<string,double>::iterator pos = varStore.find(indStrings[k]);
  //               if (pos == varStore.end()) {
  //                   //handle the error
  //               }else {
  //                   temp.append(to_string(pos->second));
  //                   temp.append(" ");
  //               }
  //             }else{
  //               temp.append(indStrings[k]);
  //               temp.append(" ");
  //             }
  //           }
  //           value = calculate(temp);  //
  //         }
  //       }else{
  //         cout<<"wrong input! cannot contain multiple equals" << endl;
  //       }  

  //       std::map<string, double>::iterator it = varStore.find(varName); 
  //       if (it != varStore.end())
  //           it->second = value;
  //       else
  //         varStore.insert(std::make_pair(varName, value));  
  //       cout<<varName << " + "<<value << endl;
  //       //Add it in the result list.
  //       result.insert(iternat++,value);
  //     }
  //   }

  //   return result;
  // }

  bool is_number(const string& s){
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

  bool is_operator(string s){
    if(s.compare("+")==0 
      || s.compare("/")==0 
      || s.compare("*")==0 
      || s.compare("-")==0 
      || s.compare("(")==0
      || s.compare(")")==0)
      return true;
    return false;
  }


  vector<string> splitString(string str){
    //cout<<"string"<<str;
    vector<string> x;
    char tab2[1024];
    strcpy(tab2, str.c_str());
    char *token = std::strtok(tab2, " =");
    while (token != NULL) {
        x.push_back((string)token);
        token = std::strtok(NULL, " =");
    }
    return x;
  }


  vector<double> calculateMulti(vector<string> expressionValues){
    std::vector<double> result;
    //Splitting the string
    // std::vector<double>::iterator iternat;
    // iternat = result.begin();
    for(int j=0; j < expressionValues.size(); j++){
      //cout<<expressionValues[j] <<"_____________"<<endl; 
      std::vector<string> individTokens = splitString(expressionValues[j]);
      //cout<<individTokens.size() <<"size------"<<endl;
      string temp="";
      //Assuming the first token is name of the variable
      std::string::size_type sz;
      string varName  = individTokens[0];
      //cout<<varName<<endl;
      double value;
      if(individTokens.size()==2){
        value = std::stod(individTokens[1],&sz);
        //cout << "value: "<<value<<endl; 
      }else{
        temp="";
          //We have an expression here
        for(int expIt = 1 ; expIt < individTokens.size(); expIt++){
          
          //Check if the variable exists in the hashtable, if not spit out error
          if(!is_number(individTokens[expIt]) && !is_operator(individTokens[expIt])){
            //Variable found
            
            std::map<string,double>::iterator pos = varStore.find(individTokens[expIt]);
            if (pos == varStore.end()) {
                //handle the error
                cout<<"Variable not defined"<<endl;
                //exit----------------------------------------------------------------------------TBD
                exit(0);
            }else {
                temp.append(to_string(pos->second));
                temp.append(" ");
            }
          }else{
            //Normal ones like number and operators
            temp.append(individTokens[expIt]);
            temp.append(" ");
          }
        }


        //Now that the expression is found, we move to compute the value of the expression
        value = calculate(temp);
      }
      //Now we need to add into the hash and to the result table
      std::map<string, double>::iterator it = varStore.find(varName); 
      if (it != varStore.end())
        it->second = value;
      else
        varStore.insert(std::make_pair(varName, value));  

      result.push_back(value);
    }

    return result;
  }  



};






#ifndef __main__

#define __main__



int main(int argc, char* argv[]) {

  Calculator cobj;

  cout << "First Step" << endl;

  // should print 5.85714

 // cout << cobj.calculate("3 + 4 * 5 / 7") << endl;



  cout << "\nSecond Step" << endl;

  // should print a 5

  //cout << cobj.calculate("( 10 + 5 ) * 9 / 4") << endl;

  //cout << cobj.calculate("( 1 + ( 4 + 5 + 2 ) - 3 ) + ( 6 + 8 )") << endl;

  cout << cobj.calculate("( ( -7.3 + -1.5 ) * ( 5 + 6 ) - 10 / 10 + 1 )") << endl;
  //cout << cobj.calculate("( -5 + 1 * ( 5 * 0.5 ) )");
  cout << "\nThird Step" << endl;



  // should print 3 and 243. 3 for "pi" and 243 for "9 * 9 * 3".

  vector<double> output = cobj.calculateMulti(

      { "pi = 0.3", "pizza = pi * ( 9 * 9 ) ", "roku = pizza / pi", "vid = pizza * 3"});

  for (size_t i = 0; i < output.size(); ++i) {

    cout << output[i] << endl;

  }

}



#endif
