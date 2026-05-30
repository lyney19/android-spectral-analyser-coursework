package com.mirea.kt.ribo.notescope;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.mirea.kt.ribo.notescope.network.HttpMethod;
import com.mirea.kt.ribo.notescope.network.HttpService;
import com.mirea.kt.ribo.notescope.network.body.ContentType;
import com.mirea.kt.ribo.notescope.network.model.ApiBody;
import com.mirea.kt.ribo.notescope.network.model.TaskResponse;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class HttpServiceDebugTest {
    HttpService service = new HttpService();

    @Test
    public void androidForStudentsJsonSuccessTest() {
        var login = "Student13741";
        var password = "TLZE8X";
        var group = "RIBO-04-24";

        request(login, password, group);
    }

    @Test
    public void androidForStudentsXmlSuccessTest() {
        var login = "Student395865";
        var password = "6MP7K0";
        var group = "RIBO-04-24";

        request(login, password, group);
    }

    @Test
    public void androidForStudentsJsonErrorTest() {
        var login = "1";
        var password = "1";
        var group = "RIBO-04-24";

        request(login, password, group);
    }

    @Test
    public void androidForStudentsXmlErrorTest() {
        var login = "s";
        var password = "s";
        var group = "RIBO-04-24";

        request(login, password, group);
    }

    private void request(String login, String password, String group) {
        var url = "https://android-for-students.ru/coursework/login.php";

        var response = service.request(
                HttpMethod.POST,
                url,
                new ApiBody(ContentType.FORM_URLENCODE, Map.of(
                        "lgn", login,
                        "pwd", password,
                        "g",   group
                )),
                TaskResponse.class
        );

        Log.i("HttpServiceDebugTest", response.toString());
    }
}
