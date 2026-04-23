package com.esports.controllers.user;

import com.esports.dao.MessageDao;
import com.esports.models.Message;
import com.esports.models.Sujet;
import com.esports.utils.ForumInputRules;
import com.esports.utils.ValidationHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.List;

public class MessageController {
    private static final int MESSAGE_MIN = 3;
    private static final int MESSAGE_MAX = 2000;

    @FXML private Label sujetTitreLabel;
    @FXML private Label composerHintLabel;
    @FXML private VBox editingPreviewBox;
    @FXML private Label editingOriginalLabel;
    @FXML private Label messageErrorLabel;
    @FXML private TextArea messageField;
    @FXML private VBox messagesContainer;
    @FXML private Button sendButton;
    @FXML private Button cancelEditButton;

    private final MessageDao messageDao = new MessageDao();
    private Sujet currentSujet;
    private Message editingMessage;

    public void setSujet(Sujet s) {
        this.currentSujet = s;
        if (sujetTitreLabel != null) {
            sujetTitreLabel.setText(s.getTitre());
        }
        refreshMessages();
    }

    @FXML
    public void initialize() {
        messageField.textProperty().addListener((o, a, b) -> clearMessageError());
    }

    private void refreshMessages() {
        if (currentSujet == null) {
            return;
        }
        List<Message> list = messageDao.getBySujet(currentSujet.getId());
        messagesContainer.getChildren().clear();

        if (list.isEmpty()) {
            Label empty = new Label("No replies yet — be the first to post below.");
            empty.getStyleClass().add("forum-msg-empty");
            empty.setMaxWidth(Double.MAX_VALUE);
            messagesContainer.getChildren().add(empty);
            return;
        }

        for (Message m : list) {
            HBox card = new HBox(14);
            card.getStyleClass().add("forum-msg-card");
            card.setPadding(new Insets(4, 6, 4, 6));

            Label avatar = new Label(avatarLetter(m.getContenu()));
            avatar.getStyleClass().add("forum-msg-avatar");
            avatar.setMinSize(40, 40);

            VBox body = new VBox(8);
            HBox.setHgrow(body, Priority.ALWAYS);

            Label text = new Label(m.getContenu());
            text.getStyleClass().add("forum-msg-text");
            text.setWrapText(true);
            text.setMaxWidth(Double.MAX_VALUE);

            HBox actions = new HBox(8);
            actions.getStyleClass().add("forum-msg-actions");
            actions.setPadding(new Insets(4, 0, 0, 0));

            Button editBtn = new Button("Edit");
            editBtn.getStyleClass().addAll("forum-msg-btn", "forum-msg-btn-edit");
            editBtn.setOnAction(e -> startEdit(m));

            Button delBtn = new Button("Delete");
            delBtn.getStyleClass().addAll("forum-msg-btn", "forum-msg-btn-del");
            delBtn.setOnAction(e -> confirmDelete(m));

            actions.getChildren().addAll(editBtn, delBtn);
            body.getChildren().addAll(text, actions);
            card.getChildren().addAll(avatar, body);
            messagesContainer.getChildren().add(card);
        }
    }

    private static String avatarLetter(String contenu) {
        if (contenu == null || contenu.isBlank()) {
            return "?";
        }
        String t = contenu.trim();
        int cp = t.codePointAt(0);
        return new String(Character.toChars(cp)).toUpperCase();
    }

    private void startEdit(Message m) {
        editingMessage = m;
        String text = m.getContenu() != null ? m.getContenu() : "";
        if (editingOriginalLabel != null) {
            editingOriginalLabel.setText(text.isEmpty() ? "(empty)" : text);
        }
        if (editingPreviewBox != null) {
            editingPreviewBox.setVisible(true);
            editingPreviewBox.setManaged(true);
        }
        if (composerHintLabel != null) {
            composerHintLabel.setText("Modify your message below");
        }
        messageField.setText(text);
        if (sendButton != null) {
            sendButton.setText("Save changes");
        }
        if (cancelEditButton != null) {
            cancelEditButton.setVisible(true);
            cancelEditButton.setManaged(true);
        }
        Platform.runLater(() -> {
            messageField.requestFocus();
            messageField.positionCaret(text.length());
        });
    }

    @FXML
    private void handleCancelEdit() {
        editingMessage = null;
        messageField.clear();
        clearMessageError();
        if (editingPreviewBox != null) {
            editingPreviewBox.setVisible(false);
            editingPreviewBox.setManaged(false);
        }
        if (composerHintLabel != null) {
            composerHintLabel.setText("Your reply");
        }
        if (sendButton != null) {
            sendButton.setText("Post reply");
        }
        if (cancelEditButton != null) {
            cancelEditButton.setVisible(false);
            cancelEditButton.setManaged(false);
        }
    }

    @FXML
    private void handleSend() {
        if (currentSujet == null) {
            return;
        }
        if (!validateMessageInput()) {
            return;
        }
        String text = messageField.getText().trim();
        if (editingMessage == null) {
            Message m = new Message();
            m.setSujetId(currentSujet.getId());
            m.setContenu(text);
            messageDao.add(m);
        } else {
            editingMessage.setContenu(text);
            messageDao.update(editingMessage);
            handleCancelEdit();
        }
        if (editingMessage == null) {
            messageField.clear();
        }
        refreshMessages();
    }

    private void confirmDelete(Message m) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete message");
        confirm.setHeaderText(null);
        confirm.setContentText("Delete this message?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                messageDao.delete(m.getId());
                if (editingMessage != null && editingMessage.getId() == m.getId()) {
                    handleCancelEdit();
                }
                refreshMessages();
            }
        });
    }

    private boolean validateMessageInput() {
        clearMessageError();
        String text = messageField.getText() != null ? messageField.getText().trim() : "";
        String err = ForumInputRules.validateReply(text, MESSAGE_MIN, MESSAGE_MAX);
        if (err != null) {
            showMessageError(err);
            return false;
        }
        return true;
    }

    private void showMessageError(String message) {
        ValidationHelper.setFieldError(messageField, true);
        if (messageErrorLabel != null) {
            messageErrorLabel.setText(message);
            messageErrorLabel.setVisible(true);
            messageErrorLabel.setManaged(true);
        }
    }

    private void clearMessageError() {
        ValidationHelper.clearFieldError(messageField);
        if (messageErrorLabel != null) {
            messageErrorLabel.setText("");
            messageErrorLabel.setVisible(false);
            messageErrorLabel.setManaged(false);
        }
    }
}
