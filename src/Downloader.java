import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Input {
	public static void main(String[] args) throws InterruptedException {

		int maxTreadCnt = 12;
		ExecutorService executor = Executors.newFixedThreadPool(maxTreadCnt);
		Scanner sc = new Scanner(System.in);

		System.out.print("다운할 주소를 입력:");
		String adr = sc.nextLine();
		int adrLength = adr.length();
		String inputAdr = adr.substring(0, adrLength - 8);
		System.out.println(inputAdr);

		System.out.print("마지막 페이지 입력 (0입력시 오래걸리지만 끝까지 알아서): ");
		int lastPage = 999;
		int tempLastPage = sc.nextInt();
		if (tempLastPage != 0) {
			lastPage = tempLastPage;
		}
		sc.nextLine();
		System.out.println(lastPage);

		System.out.print("세션값 입력 (JSESSIONID_HAKSAF= 이후 값) :");
		String session = sc.nextLine();
		System.out.println(session);

		System.out.print("저장할 위치 입력: ");
		String save = sc.nextLine();
		System.out.println(save);

		// *****************************중요***********************************************
		// b 는 시작페이지 1 ~ 마지막 페이지 번호
		for (int b = 1; b <= lastPage; b++) {
			String filenameString = String.format("%04d", b);

			Thread.sleep(1);
			Runnable down = new Down(filenameString, inputAdr, save, session);
			executor.submit(down);

		}

	}
}

class Down implements Runnable {
	String filenameString, adr, save, sesstion;

	public Down(String filenameString, String adr, String save, String session) {

		this.filenameString = filenameString;
		this.adr = adr;
		this.save = save;
		this.sesstion = session;

	}

	@Override
	public void run() {

		// *****************************중요***********************************************
		// 사이트에서 개발자 모드를 통해 다운받을 주소중 파이지 번호만 빼고 넣을것
		String spec = adr + filenameString + ".jpg";
		String outputDir = save;
		InputStream is = null;
		FileOutputStream os = null;
		try {
			URL url = new URL(spec);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			// *****************************중요***********************************************
			// 헤더의 쿠키중 세션아이디를 입력
			conn.setRequestProperty("Cookie", "JSESSIONID_HAKSAF=" + sesstion);

			int responseCode = conn.getResponseCode();

			// Status 가 200 일 때
			if (responseCode == HttpURLConnection.HTTP_OK) {
				String fileName = "";
				String disposition = conn.getHeaderField("Content-Disposition");
				String contentType = conn.getContentType();

				// 일반적으로 Content-Disposition 헤더에 있지만
				// 없을 경우 url 에서 추출해 내면 된다.
				if (disposition != null) {
					String target = "filename=";
					int index = disposition.indexOf(target);
					if (index != -1) {
						fileName = disposition.substring(index + target.length() + 1);
					}
				} else {
					fileName = spec.substring(spec.lastIndexOf("/") + 1);
				}

				System.out.println("Content-Type = " + contentType);
				System.out.println("Content-Disposition = " + disposition);
				System.out.println("fileName = " + fileName);

				is = conn.getInputStream();
				os = new FileOutputStream(new File(outputDir, fileName));

				final int BUFFER_SIZE = 16384;
				int bytesRead;
				byte[] buffer = new byte[BUFFER_SIZE];
				while ((bytesRead = is.read(buffer)) != -1) {
					os.write(buffer, 0, bytesRead);
				}
				os.close();
				is.close();
				System.out.println(spec + " downloaded");
			} else {
				System.out.println(spec);
				System.out.println(spec + " Fail");
			}
			conn.disconnect();
		} catch (Exception e) {
			System.out.println("errpr: " + spec);
			System.out.println("An error occurred while trying to download a file.");
			e.printStackTrace();
			try {
				if (is != null) {
					is.close();
				}
				if (os != null) {
					os.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}
}
