package org.game.events;

@FunctionalInterface
public interface MoneyEventHandler {
    void handleMoneyChanged(int newAmount);
}