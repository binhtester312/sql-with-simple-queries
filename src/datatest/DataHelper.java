package com.nopcommerce.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.io.IOException;

public class DataHelper {

    @DataProvider(name = "invalidLoginData")
    public static Object[][] getInvalidLoginData() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = new File("src/test/resources/data/login_invalid_data.json");

        // 1. Chuyển file JSON thành mảng đối tượng LoginData[]
        LoginData[] dataArray = mapper.readValue(jsonFile, LoginData[].class);

        // 2. Chuyển sang mảng 2 chiều Object[][] để phù hợp với TestNG DataProvider
        Object[][] data = new Object[dataArray.length][1];
        for (int i = 0; i < dataArray.length; i++) {
            data[i][0] = dataArray[i];
        }
        return data;
    }
}