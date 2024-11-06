package run_time_db;

import packet.Command;
import packet.Packet;
import packet.User;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public enum UserManagement {
    INSTANCE;

    public List<User> users;

    UserManagement() {
        this.users = List.of(
                User.builder().nickname("Mock 1").password("1234").build(),
                User.builder().nickname("Mock 2").password("1234").build(),
                User.builder().nickname("Mock 3").password("1234").build()
        );
    }

    public void register() {
        /* TODO: Implement */
    }

    public Optional<User> findUserByUsername(String username) {
        return users.stream()
                .filter(user -> user.getNickname().equals(username))
                .findFirst();
    }

    public Optional<User> login(User userToLogin) {
        return this.users
                .stream()
                .filter(user -> user.equals(userToLogin))
                .findFirst();
    }

    public void broadcastMessage(Packet packet) {
        for (User user : users) {
            /* Do not send the message back to the user who sent it */
            boolean isNotSameUser = !packet.getUser().getNickname().equals(user.getNickname());

            if (Objects.nonNull(user.getSocket()) && isNotSameUser) {
                User cleanUser = User.builder()
                        .nickname(packet.getUser().getNickname())
                        .build();

                Packet messagePacket = Packet.builder()
                        .user(cleanUser)
                        .message(packet.getMessage())
                        .command(Command.MESSAGE_ALL)
                        .build();

                try {
                    ObjectOutputStream userOutStream = user.getOutStream();
                    if (userOutStream != null) {
                        userOutStream.writeObject(messagePacket);  // Use the user's existing stream
                        userOutStream.flush();
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Error sending message to user: " + user.getNickname(), e);
                }
            }
        }
    }
    public void sendMessageToUser(User sender, User recipient, String message) {
        if (recipient.getOutStream() != null) {
            // Create a packet for the individual message
            Packet messagePacket = Packet.builder()
                    .user(User.builder().nickname(sender.getNickname()).build())  // Sender's nickname
                    .message(message)
                    .command(Command.MESSAGE_INDIVIDUAL)
                    .build();

            try {
                recipient.getOutStream().writeObject(messagePacket);  // Send the message to the recipient
                recipient.getOutStream().flush();
            } catch (IOException e) {
                throw new RuntimeException("Error sending individual message to user: " + recipient.getNickname(), e);
            }
        }
    }

}