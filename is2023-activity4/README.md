# Activitat 4

En aquesta activitat, tot _inspirats_ pels exemples del projecte `jmgimeno/zio-restful-webservice`, que podeu trobar al repositori <https://github.com/jmgimeno/zio-restful-webservice>, es demana fer un servei web que permeti controlar una `Machine` de venda de _xiclets de meló_.

A continuació descriuré breument l'estructura del projecte i el que heu de fer (com sempre. partirem d'un esquelet inicial que haureu de completar)

A l'informe, com sempre, expliqueu-me què heu fet i perquè, quines dificultats us heu trobat, com les heu resolt, què heu consultat, etc, etc.

## Paquet `candy`

En aquest paquet teniu el codi de la màquina de xuxeries que vam veure al tema sobre `State`, així com la implementació de la classe de les _state actions_. Fixeu-vos que el codi de la màquina és una __classe immutable__.

Al mateix paquet, però al directori de testos, he migrat el codi de test que apareix al llibre a `zio-test`.

El codi d'aquest paquet no l'heu de modificar.

## Paquet `services`

Com ala implementació que tenim de la màquina és immutable, de cara a que es pugui usar des de l'aplicació web, haurem de crear un servei per a representar fluxos de treball que, respectant la transparència referencial, descriguin modificacions d'aquesta.

Per això definirem la interfície del servei com:

```scala
trait CandyService:
  def coin: ZIO[Any, Nothing, Unit]
  def turn: ZIO[Any, Nothing, Int]
  def status: ZIO[Any, Nothing, MachineStatus]
```

Amb el següent efecte:

- `coin`: és un flux que actualitza l'estat de la màquina fent que la seva entrada sigui una moneda (`Coin`)
- `turn`: és un flux que actualitza l'estat de la màquina fent que la seva entrada sigui girar la maneta (`Turn`). Retorna el número de `melonGums` que resten a la màquina després de fer l'acció.
- `status`: és un flux que retorna l'estat de la màquina, que ve representat per aquesta classe (que es pot codificar en _json_):

  ```scala
  final case class MachineStatus(locked: Boolean, melonGums: Int)

  object MachineStatus:
    given JsonEncoder[MachineStatus] = 
      DeriveJsonEncoder.gen[MachineStatus]
  ```

De cara a simplificar el codi d'altres fluxos que usin aquests servei quan està a les seves dependències, a l'_objecte company_ de la interfície definirem:

```scala
object CandyService:
  def coin: ZIO[CandyService, Nothing, Unit]            = ???
  def turn: ZIO[CandyService, Nothing, Int]             = ???
  def status: ZIO[CandyService, Nothing, MachineStatus] = ???
```

## Paquet `services.life`

Aquí implementarem una instància del servei utilitzant una __referència mutable__ a una màquina, és a dir:

```scala
final case class CandyServiceLive(ref: Ref[Machine]) extends CandyService:
  def coin: ZIO[Any, Nothing, Unit]            = ???
  def turn: ZIO[Any, Nothing, Int]             = ???
  def status: ZIO[Any, Nothing, MachineStatus] = ???
```

De cara a construir una implementació per usar-la com a dependència d'un altre servei, crearem el següent mètode al seu _objecte company_:

```scala
object CandyServiceLive:
  def layer(melonGums: Int): ZLayer[Any, String, CandyServiceLive] = ???
```

Fixeu-vos en que, com el `layer` no depèn de res, haurà de crear la referència que contindrà la màquina.

La màquina que es crearà, tindrà el nombre de `melonGums` que se li passen (que no podrà ser negatiu), estarà _desbloquejada_ i no contindrà cap moneda.

Si el nombre passat és negatiu, la construcció fallarà amb un error de tipus `String`.

## Paquet `api`

Aquest paquet conté a definició de la api http que conindrà els següent endpoints:

```scala
object CandyAPI:
  def apply(): Http[CandyService, Throwable] = ???
```

- `GET` a `/status`:  retorna, en forma de _json_, l'status actual de la màquina

- `POST` a `/coin`: realitza l'acció de posar una moneda a la màquina i retorna l'status http `ok`

- `POST` a `/turn`: realitza l'acció de girar la maneta i retorna el número de `melonGums` que té la màquina

## Paquet `main`

Representa el programa principal que serveix la api al port `8080` i que inicialitza la màquina amb `100` xiclets de melò.

```scala
object CandyMain extends ZIOAppDefault:
  val run: ZIO[Any, String | Throwable, Nothing] = ???
```

## Possibles ampliacions

La part obligatòria de la pràctica és el que he comentat fins ara, però hi ha força coses que es podem millorar i explorar. Aquí us faig suggerències, per si voleu explorar alguna coseta:

### Control d'errors

- Implementar en les màquines (classe `Candy`) un concepte d'error per tal de representar accions invàlides:
  - qualsevol cosa sobre una màquina sense xuxes
  - agafar moneda en desbloquejada
  - girar moneda en la màquina desbloquejada

- Una vegada fet lo anterior, aquests errors (controlats) també modficaràn el servei i la seva implementació. A més l'API haurà d'indicar-los (amb un status de bad request)

### Vàries màquines

- Una cosa que es pot fer també, amb o sense la modificació anterior, es tenir una aplicació web per crear i controlar vàries màquines (seguint un esquema simular al del l'aplicació web amb users).

- Inicialment podeu fer servir un repositori de màquines en memòria i, ja més complicar, un usant una base de dades.
