package com.gamesUP.gamesUP.service;

import com.gamesUP.gamesUP.dto.game.GameRequest;
import com.gamesUP.gamesUP.dto.game.GameResponse;
import com.gamesUP.gamesUP.dto.game.GameSearchCriteria;
import java.util.List;

public interface GameService {

    List<GameResponse> search(GameSearchCriteria criteria);

    GameResponse findById(Long id);

    GameResponse create(GameRequest request);

    GameResponse update(Long id, GameRequest request);

    void delete(Long id);
}
