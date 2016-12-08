//servo és ultrahang könyvtár
#include <Servo.h>
#include <NewPing.h>

//bluetooth beállítás
#define BUFFERSIZE 22 //byte
#define BTTIMEOUT 20 //ms
//az alábbi műveletre azért van szükség, mert feltorlódnak a parancsok, ha sokat dolgozunk fel egyszerre
#define BTMAXCOMMAND 3 //hány darab külső parancs után ürítse ki a bejovoAdatot

//servo beállítások
int view = 120; //max 180;
int kezdo = 90 - (view / 2); //a max 180-nál: 180/2=90 //most 30
int veg = kezdo + view; //most 150
int irany = 1;
int szog = kezdo;
int vezerlojel = 7; //D7 PWM servo vezerlojel Megán
Servo srv;

//motor miatt
int Men_A = 8;
int MA_1 = 9;
int MA_2 = 10;
int MB_1 = 12;
int MB_2 = 11;
int Men_B = 13;
int sebesseg = 0;
float balMotorDifferencia = 1.0;
float jobbMotorDifferencia = 1.12;
bool eloreVhatra = true; //true az előre

//encoder tárcsa
int MEGSZ_1 = 20;
int MEGSZ_2 = 21;
int balJelSzam = 0;
int jobbJelSzam = 0;

//ultrahang beállítások
#define TRIGGER 6 //trigger láb D6 Megán
#define ECHO 5 //echo láb D5 Megán
#define MAX_TAV 200 //most azt szeretném, hogy 2 méterre lásson el
NewPing sonar(TRIGGER, ECHO, MAX_TAV);

void setup() {
  //Konfighoz:
  //Serial.begin(38400);
  //Serial3.begin(38400);
  //Alapjáraton:
  Serial.begin(9600);//író BT-ra
  Serial3.begin(9600);//olvasó BT-ról
  Serial3.setTimeout(BTTIMEOUT);//eddig várunk adatra a bluetooth-ról maximum, utána megszakítjuk

  //servo felcsatolása
  srv.attach(vezerlojel);

  //motor miatt
  //pwm sebesség A és B motor
  pinMode(Men_A, OUTPUT);
  pinMode(Men_B, OUTPUT);
  //high/low forgás irány szabályozók
  pinMode(MA_1, OUTPUT);
  pinMode(MA_2, OUTPUT);
  pinMode(MB_1, OUTPUT);
  pinMode(MB_2, OUTPUT);

  //encoder miatt
  pinMode(MEGSZ_1, INPUT);
  pinMode(MEGSZ_2, INPUT);
  attachInterrupt(digitalPinToInterrupt(MEGSZ_1), balJelSzam++, FALLING);
  attachInterrupt(digitalPinToInterrupt(MEGSZ_2), jobbJelSzam++, FALLING);
}

//jobbat hányszor kell felszorozni a balhoz
void sebessegDiff(){
  float hanyados= balJelSzam / jobbJelSzam;
  balJelSzam = 0;
  jobbJelSzam = 0;
  //balMotorDifferencia = 1.0;
  jobbMotorDifferencia = hanyados;
}

//sebesség 0-255
void balSebesseg(int seb) {  
  seb = (float)(85 * balMotorDifferencia);
  analogWrite(Men_A, seb);
}

//sebesség 0-255
void jobbSebesseg(int seb) {
  seb = (float)(85 * jobbMotorDifferencia);
  analogWrite(Men_B, seb);
}

void balElore() {
  digitalWrite(MA_1, LOW);
  digitalWrite(MA_2, HIGH);
}

void jobbElore() {
  digitalWrite(MB_1, LOW);
  digitalWrite(MB_2, HIGH);
}

void balHatra() {
  digitalWrite(MA_1, HIGH);
  digitalWrite(MA_2, LOW);
}

void jobbHatra() {
  digitalWrite(MB_1, HIGH);
  digitalWrite(MB_2, LOW);
}

void balAllj() {
  digitalWrite(MA_1, LOW);
  digitalWrite(MA_2, LOW);
}

void jobbAllj() {
  digitalWrite(MB_1, LOW);
  digitalWrite(MB_2, LOW);
}

//a beérkező adatok figyelése
String bejovoAdat;
void BT_beerkezo() {
  //akár jön adat a sebességgel kapcsolatban, akár nem nullázzuk ki
  //így ha nem jön új adat, akkor nem ragad be a régi és nem megy magától a ketyere
  sebesseg = 0;

  // put your main code here, to run repeatedly:
  //ez addig futott, amíg csak volt adat
  //while(Serial3.available()>0){
  //most csak egyszer olvas be a lejárati időig, ami alapértéken 1másodperc lenne (átállítottam 200ms-ra), vagy ha már nincs több adat
  if (Serial3.available() > 0) {
    //ez most addig olvas amíg timeout-ra nem fut, de ezt most lecseréljük
    //String s = Serial3.readString();
    //addig olvas, amíg a mi üzenetstruktúránk szerinti üzenetvége jelet nem kap, vagy timeot-ra nem fut
    //de ez lecsípi az üzenet végéről a vége jelet, így ezt sem jó számunkra
    //String s = Serial3.readStringUntil('|');
    //10 byte adatot olvasunk be, vagy timeout-ig
    char buf[BUFFERSIZE];
    Serial3.readBytes(buf, BUFFERSIZE);
    for (int j = 0; j < BUFFERSIZE; j++) {
      if (buf[j] != '\0') bejovoAdat += buf[j];
    }
    //bejovoAdat += s;
    ////Serial.println("Received from RX3: " + s);

    /*
      Ez amellett hogy soros monitoron megjeleníti, hogy mit kap,
      egyben vissza is küldeni a BT-nak amit kapott, mert
      ezen a TX0-ás porton írunk a BT-ra is.
    */
    //tesztelésnél itt kibukik, hogy néha szemét adat is átjön, de ezzel most nem foglalkozunk, mert
    //amint utána átjön egy jó adat a szemét adat is kitörlődik a behovoAdat változóból
    //Serial.println(s);
  }

  //tesztelés szempontjából
  //Serial.println(bejovoAdat);

  /*
    csomag kezdés jel >
    csomag lezáró jel |
    adat szeparátor jel #

    minden üzenetnek van csomagtípusa!!!
    csomagtípusok:
    bejovok:
    0: tesztüzenet: üzenet
    1: motorvezértlő: szög
    2: motorvezérlő: sebesség
    kimenok:
    3: ultrahang: szög(fok), távolság(cm), irány (merre tart a radar)
    irányból 1 az alap, -1 ha visszafordul

    tehát a csomag maximum 4 részből áll
    típusa, plusz 3 adat

    csomag szerkezete:
    >csomagtípus#adat1#adat2#adatn|
  */
  int vanKezdo = bejovoAdat.indexOf('>');
  int vanLezaro = bejovoAdat.indexOf('|');
  //felgyűlnek az adatok, ha addig dolgozzuk fel a parancsokat, amíg vannak
  //while(vanLezaro > -1){
  //sajnos így nem lehet real time, addig dolgozzunkk, amíg van parancs, de maximum 5 parancsig
  //utánna ürítsük ki a bejovoAdat változót
  int hanyadikParancs = 0;
  while ((vanLezaro > -1) && (hanyadikParancs <= BTMAXCOMMAND)) {
    if (vanKezdo > -1) {
      //tömb létrehozása a csomagnak
      //annyi a hossza ahány a csomag maximálix elemszáma
      String csomag[4];
      //van lezáró és kezdő jelünk is, daraboljuk fel az üzenetünket
      String feldolgozando = bejovoAdat.substring(vanKezdo + 1, vanLezaro);
      //Serial.println("Teszt: "+feldolgozando);
      int sorszam = 0;
      int honnan = 0;
      int elvalaszto = feldolgozando.indexOf('#', honnan);
      while (elvalaszto > -1) {
        csomag[sorszam] = feldolgozando.substring(honnan, elvalaszto);
        //Serial.println("Teszt: "+csomag[sorszam]);
        //a következő csomgarészhez
        sorszam++;
        honnan = elvalaszto + 1;
        elvalaszto = feldolgozando.indexOf('#', honnan);
      }
      //ha nincs már elválasztó, akkor ez az utolsó csomagdarabka
      csomag[sorszam] = feldolgozando.substring(honnan, feldolgozando.length());
      //Serial.println("Teszt: "+csomag[sorszam]);
      //levesszük a most feldolgozott bejovő adatot az elejéről
      bejovoAdat = bejovoAdat.substring(vanLezaro + 1, bejovoAdat.length());
      //kivesszük a parancsot és annak megfelelően cselekszünk
      String mit;
      int seged = 0;
      String teszt = "";
      switch ((csomag[0]).toInt()) {
        case 0:
          mit = csomag[1];
          if (mit == "on")
            Serial.println("onOK");
          else if (mit == "off")
            Serial.println("offOK");
          else
            Serial.println("ERROR");
          break;
        case 1:
          break;
        case 2:
          mit = csomag[1];
          seged = mit.toInt();
          if (seged >= 0) {
            eloreVhatra = true;
          } else {
            eloreVhatra = false;
          }
          seged = abs(seged);
          //itt csak egy globális értéket adunk a sebességnek, de nem itt motorozunk
          sebesseg = map(seged, 0, 100, 0, 255);
          //teszt=String(seged)+":"+String(sebesseg);
          //Serial.println(teszt);
          break;
        default:
          Serial.println("ERROR");
          break;
      }
      hanyadikParancs++;
    } else {
      //ha van lezáró jel, de kezdő nincs, akkor a csomag eleje elveszett
      //töröljük ki az első lezárójelig a bejövő adatot
      bejovoAdat = bejovoAdat.substring(vanLezaro + 1, bejovoAdat.length());
    }
    //az új csomaghoz
    vanKezdo = bejovoAdat.indexOf('>');
    vanLezaro = bejovoAdat.indexOf('|');
  }

  //ha túl sok adat van a bejovoAdatokban, akkor késleltetés alakul ki és tovább működik a motor, mint kéne
  //ezért ha több adat maradt feldolgozás után a bejovoAdatokban, mint amennyit a bufferben most ujonnan beolvastunk
  //ami csak akkor állhat fent, ha az új beolvasott adatokkal sem tudtunk értelmezni egy parancsot sem a szövegben
  //akkor kiürítjük a bejovoAdatokat
  //if(bejovoAdat.length()>BUFFERSIZE) bejovoAdat="";
  //mindenféleképpen ürítsünk, mert közben állítottunk be maximum parancsok számát
  bejovoAdat = "";

  /*
    Ez akkor kellene, ha a soros monitorból szeretnénk írni a BT-nak.
  */
  /*
    while(Serial.available()>0){
    String s = Serial.readString();
    //Serial.println("Sent to TX0: " + s);
    Serial.println(s);
    Serial3.println(s);
    }
  */
}

//ultrahang és servo lekezelése
void UltraServ() {
  unsigned int tav;

  srv.write(szog);
  delay(2);
  tav = sonar.ping_median();
  delay(145); //4*29

  //a telefonon pont fordítva van a megjelenítés, ezért a szöget tükröznünk kell a skálán
  //és ezért az irány is fordított -irany
  //int szog2=szog;
  int szog2 = 180 - szog;

  //küldés BT-on
  Serial.println(">3#" + (String)(szog2) + "#" + (String)(tav / US_ROUNDTRIP_CM) + "#" + (-irany) + "|");

  if (irany == 1) {
    //növelem a szöget a következő iterációhoz
    szog++;

    //ha elértük a szélső határt akkor a következő iterációnál irányt váltunk
    if (szog == veg + 1) irany = -irany;
  } else {
    //csökkentem a szöget a következő iterációhoz
    szog--;

    //ha elértük a szélső határt akkor a következő iterációnál irányt váltunk
    if (szog == kezdo - 1) irany = -irany;
  }
}

void loop() {
  UltraServ();

  BT_beerkezo();

  //a távirányítós autó tesztelése
  if (eloreVhatra) {
    balElore();
    jobbElore();
  } else {
    balHatra();
    jobbHatra();
  }
  if (sebesseg == 0) {
    balAllj();
    jobbAllj();
  }

  sebessegDiff();
  balSebesseg(sebesseg);
  jobbSebesseg(sebesseg);

  //azért várakoztatunk 1mp-et, hogy addig ezzel a sebességgel forogjon a kerék
  //és azért töröljük a bejövö adatokat, mert ekközben a sok forgalom miatt felgyülhetett a sok adat
  //delay(100);
  //bejovoAdat="";
  //miután ment amennyit akartuk, leállítjuk
  //sebesseg=0;
  //balAllj();
  //jobbAllj();
}
