package comasky.rpcClass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// Lombok gère les accesseurs et mutateurs
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RpcResponse<T> {

    // Le résultat de la commande RPC (sera une List<PeerInfo>)
    private T result;

    // Le champ d'erreur en cas d'échec
    private Object error;

    // L'ID de la requête (non utilisé ici)
    private String id;
}