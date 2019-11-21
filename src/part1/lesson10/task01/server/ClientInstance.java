package part1.lesson10.task01.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * @author KhafizovAR on 21.11.2019.
 * @project Stc20JavaMiddle
 */
public class ClientInstance extends Thread {

    protected DateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");

    protected final DataInputStream dis;
    protected final DataOutputStream dos;
    protected final Socket s;
    protected final Server server;

    protected String clientName;
    protected boolean isRegistered = false;

    public ClientInstance(Socket s, DataInputStream dis, DataOutputStream dos, Server server) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.server = server;
    }

    protected void registration() throws IOException {
        String received;
        while(true) {
            this.dos.writeUTF("Для регистрации в чате наберите  #name Ваше_имя;");
            received = this.dis.readUTF();
            StringTokenizer st = new StringTokenizer(received, "# ");
            if (st.nextToken().equals("name")) {
                String newName = st.nextToken();
                this.isRegistered = true;
                for (Object mc : this.server.getConnectedClients()) {
                    if(((ClientInstance)mc).getName().equals(newName)) {
                        dos.writeUTF("Клиент с таким именеме уже существует!");
                        this.isRegistered = false;
                        break;
                    }
                }
                if(this.isRegistered) {
                    this.clientName = newName;
                    this.sendMessage("Поприветствуем " + this.clientName);
                    this.dos.writeUTF("Вы зарегистрированы под именем:" + this.clientName);
                    return;
                }
            }
            dos.writeUTF("Команда не распознана:" + received);
        }
    }

    /**
     * Отправка сообщения, в данном случае массовая рассылка
     * @param message
     * @throws IOException
     */
    protected void sendMessage(String message) throws IOException {
        System.out.println("old send message");
        this.server.broadCastMessage("[" + this.clientName + "] - [" + timeFormat.format(new Date()) + "]: " + message);
    }


    @Override
    public void run() {
        String received;
        try {
            this.registration();
            dos.writeUTF("Вы в чате. можете общаться (для выхода наберите 'quit')");
            while (true) {
                received = dis.readUTF();
                if (received.equalsIgnoreCase("quit")) {
                    System.out.println("Client " + this.s + " sends exit...");
                    this.sendMessage("Выходит из чата");
                    this.s.close();
                    System.out.println("Connection closed");
                    this.server.clientClosed(this);
                    break;
                }
                this.sendMessage(received);
            }
        }  catch (IOException e) {
            e.printStackTrace();
        }


        try {
            this.dis.close();
            this.dos.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public String getClientName() {
        return clientName;
    }
}
