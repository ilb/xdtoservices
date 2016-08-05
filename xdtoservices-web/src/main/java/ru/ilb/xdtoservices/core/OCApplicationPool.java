/*
 * Copyright 2016 slavb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.ilb.xdtoservices.core;

import com.ipc.oce.ApplicationDriver;
import com.ipc.oce.OCApp;
import com.ipc.oce.PropertiesReader;
import com.ipc.oce.exceptions.ConfigurationException;
import java.io.IOException;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.jinterop.dcom.common.JIException;
import org.springframework.stereotype.Component;

/**
 *
 * @author slavb
 */
@Component
public class OCApplicationPool {

    private int sessionID = -1;

    public synchronized OCApp getApplication() throws IOException, ConfigurationException, JIException {
        OCApp app1 = null;

        if (sessionID != -1) { // получение инстанса из хранилища
            app1 = OCApp.getInstance(sessionID);
        }

        if (app1 == null) { // если sessionID == -1 или сессия удалена
            sessionID = createConnection();
            app1 = OCApp.getInstance(sessionID);
        } else if (!(app1.ping())) {
            try {
                app1.exit();
            } catch (Exception e) { // да и хрен с нимим с битыми ссылками
            }
            sessionID = createConnection();
            app1 = OCApp.getInstance(sessionID);
        }
        return app1;
    }

    private int createConnection() throws IOException, JIException, ConfigurationException {

        // check init parameters
        Properties configuration = new Properties();
        try {
            Context ctx = new InitialContext();
            configuration.put(PropertiesReader.OCE_CFG_DRIVER, (String) ctx.lookup(PropertiesReader.OCE_CFG_DRIVER));
            configuration.put(PropertiesReader.OCE_CFG_HOST, (String) ctx.lookup(PropertiesReader.OCE_CFG_HOST));
            configuration.put(PropertiesReader.OCE_CFG_HOST_USER, (String) ctx.lookup(PropertiesReader.OCE_CFG_HOST_USER));
            configuration.put(PropertiesReader.OCE_CFG_HOST_PASSWORD, (String) ctx.lookup(PropertiesReader.OCE_CFG_HOST_PASSWORD));
            configuration.put(PropertiesReader.OCE_CFG_1CDB_PATH, (String) ctx.lookup(PropertiesReader.OCE_CFG_1CDB_PATH));
            configuration.put(PropertiesReader.OCE_CFG_1CDB_USER, (String) ctx.lookup(PropertiesReader.OCE_CFG_1CDB_USER));
            configuration.put(PropertiesReader.OCE_CFG_1CDB_PASSWORD, (String) ctx.lookup(PropertiesReader.OCE_CFG_1CDB_PASSWORD));
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }

        // else create new connection
        ApplicationDriver driver = ApplicationDriver.loadDriver(configuration
                .getProperty(PropertiesReader.OCE_CFG_DRIVER));

        OCApp app = OCApp.getNewInstance();

        app.setApplicationDriver(driver);
        int sessionCode = app.connect(configuration);

        return sessionCode;
    }
    protected void destroy() {
        exit();
    }

    public void exit() {
        if (sessionID != -1) {
            try {
                OCApp.getInstance(sessionID).exit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
