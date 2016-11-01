package Stubs;

import java.util.ArrayList;

import com.knoll.tos.F684102JE;

public class Test01 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		F684102JE error = new F684102JE();
		ArrayList<F684102JE> bunch = new ArrayList<F684102JE>();
		
		error.setQxitm(123);
		error.setQxlitm("test 123");
		error.setQxuser("you you");
		bunch.add(error);
		
		F684102JE error1 = new F684102JE();
		error1.setQxitm(456);
		error1.setQxlitm("test 456");
		error1.setQxuser("i me me mine");
		bunch.add(error1);
		
		for (F684102JE to :bunch) {
			
			System.out.println(to.getQxitm());
			System.out.println(to.getQxlitm());
			System.out.println(to.getQxuser());
		}

	}

}
