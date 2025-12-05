# Frontend Tests

Suite de tests complète pour le dashboard Bitcoin Node utilisant Vitest et Vue Test Utils.

## Configuration

### Dépendances
- **Vitest**: Framework de test moderne et rapide
- **@vue/test-utils**: Utilitaires pour tester les composants Vue
- **happy-dom**: Environnement DOM léger pour les tests
- **@vitest/ui**: Interface utilisateur pour visualiser les tests

### Installation
```bash
cd src/main/web
npm install
```

## Exécution des tests

### Lancer tous les tests
```bash
npm test
```

### Mode watch (développement)
```bash
npm test -- --watch
```

### Interface utilisateur
```bash
npm run test:ui
```

### Coverage
```bash
npm run coverage
```

## Structure des tests

### Composables (`src/composables/__tests__/`)
- **useWebSocket.test.ts**: Tests du composable WebSocket
  - Connexion/déconnexion
  - Gestion des messages
  - Reconnexion automatique avec backoff exponentiel
  - Gestion des erreurs RPC
  
- **useTheme.test.ts**: Tests de gestion du thème
  - Toggle dark/light mode
  - Persistance dans localStorage
  - Respect des préférences système
  - Mise à jour des variables CSS

### Composants (`src/components/__tests__/`)
- **Status.test.ts**: Tests du composant de statut
  - Affichage des états de connexion
  - Messages d'erreur
  - Mise à jour dynamique

### Types (`src/types/__tests__/`)
- **types.test.ts**: Validation des types TypeScript
  - GeneralStats
  - BlockChainInfo
  - NodeInfo
  - Peer
  - SubverDistribution
  - Normalisation des données

### Utilitaires (`src/utils/__tests__/`)
- **formatters.test.ts**: Tests des fonctions de formatage
  - Formatage de nombres
  - Formatage de dates
  - Formatage de pourcentages
  - Calcul du hash rate

### Logique métier (`src/__tests__/`)
- **logic.test.ts**: Tests de la logique applicative
  - Reconnexion WebSocket
  - Validation des données
  - Filtrage des peers
  - Préparation des données pour les graphiques

## Couverture des tests

Les tests couvrent :
- ✅ Composables (useWebSocket, useTheme)
- ✅ Composants Vue (Status)
- ✅ Types et validation
- ✅ Formatters et utilitaires
- ✅ Logique métier

### Objectif de couverture
- Statements: > 80%
- Branches: > 80%
- Functions: > 80%
- Lines: > 80%

## Bonnes pratiques

1. **Tests isolés**: Chaque test est indépendant
2. **Mocking**: WebSocket et localStorage sont mockés
3. **Timers**: Utilisation de `vi.useFakeTimers()` pour contrôler le temps
4. **Assertions claires**: Messages d'erreur explicites
5. **Organisation**: Tests groupés par fonctionnalité

## Exemples

### Test d'un composable
```typescript
it('should connect successfully', async () => {
  const onData = vi.fn();
  const { isConnected, connect } = useWebSocket('ws://localhost', onData);

  connect();
  await vi.runAllTimersAsync();

  expect(isConnected.value).toBe(true);
});
```

### Test d'un composant
```typescript
it('should display connected status', () => {
  const wrapper = mount(Status, {
    props: {
      isConnected: true,
      rpcConnected: true,
      errorMessage: null,
    },
  });

  expect(wrapper.text().toLowerCase()).toContain('connected');
});
```

## CI/CD

Les tests sont automatiquement exécutés lors du build:
```bash
npm run build  # Lance aussi les tests TypeScript
```

## Dépannage

### Tests qui échouent
1. Vérifier que les dépendances sont installées: `npm install`
2. Nettoyer le cache: `npx vitest --clearCache`
3. Vérifier la version de Node: Node 18+ requis

### Coverage incomplet
1. Ajouter des tests pour les branches non couvertes
2. Vérifier les exclusions dans `vitest.config.ts`

## Ressources

- [Vitest Documentation](https://vitest.dev/)
- [Vue Test Utils](https://test-utils.vuejs.org/)
- [Testing Library Best Practices](https://kentcdodds.com/blog/common-mistakes-with-react-testing-library)
