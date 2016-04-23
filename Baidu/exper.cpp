#include <iostream>
#include <string>
#include <sstream>
#include <algorithm>
#include <iterator>
#include <cstring>
using namespace std;

int main() {
 //    using namespace std;
 //    string sentence = "( ( 10 + 1 ) * 5 / 6 )";
 //    istringstream iss(sentence);
 //    vector<string> tokens;
	// copy(istream_iterator<string>(iss),
 //    	 istream_iterator<string>(),
 //     	 back_inserter(tokens));

	// for(int i = 0; i<tokens.size(); i++){
	// 	cout << tokens[i] << endl;
	// }
	//string tmp = "cat";
	

	string str = "pi = 3 * 4 + / 3 4 5";
	char tab2[1024];
	strcpy(tab2, str.c_str());
    char *token = std::strtok(tab2, " =");
    while (token != NULL) {
        std::cout << token << endl;
        token = std::strtok(NULL, " =");
    }
}