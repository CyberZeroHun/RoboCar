//servo és ultrahang könyvtár
#include <Servo.h>
#include <NewPing.h>

//servo beállítások
int view = 120; //max 180;
int kezdo = 90 - (view/2); //a max 180-nál: 180/2=90 //most 30
int veg = kezdo + view; //most 150
int szog = kezdo;
int vezerlojel = 7; //D7 PWM servo vezerlojel Megán
Servo srv;

//ultrahang beállítások
#define TRIGGER 6 //trigger láb D6 Megán
#define ECHO 5 //echo láb D5 Megán
#define MAX_TAV 450
NewPing sonar(TRIGGER, ECHO, MAX_TAV);

void setup() {
  //Konfighoz:
  //Serial.begin(38400);
  //Serial3.begin(38400);
  //Alapjáraton:
  Serial.begin(9600);//író BT-ra
  Serial3.begin(9600);//olvasó BT-ról

  //servo felcsatolása
  srv.attach(vezerlojel); 
}

//a beérkező adatok figyelése
String bejovoAdat;
void BT_beerkezo(){
// put your main code here, to run repeatedly:
  while(Serial3.available()>0){
    String s = Serial3.readString();
    bejovoAdat += s;
    ////Serial.println("Received from RX3: " + s);
    
    /*
    Ez amellett hogy soros monitoron megjeleníti, hogy mit kap,
    egyben vissza is küldeni a BT-nak amit kapott, mert
    ezen a TX0-ás porton írunk a BT-ra is.
    */
    //Serial.println(s);
       
  }

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
  while(vanLezaro > -1){    
    if(vanKezdo > -1){
      //tömb létrehozása a csomagnak
      //annyi a hossza ahány a csomag maximálix elemszáma
      String csomag[4];
      //van lezáró és kezdő jelünk is, daraboljuk fel az üzenetünket
      String feldolgozando = bejovoAdat.substring(vanKezdo+1, vanLezaro);
      //Serial.println("Teszt: "+feldolgozando);
      int sorszam=0;
      int honnan=0;
      int elvalaszto= feldolgozando.indexOf('#',honnan);
      while(elvalaszto > -1){
        csomag[sorszam]=feldolgozando.substring(honnan,elvalaszto);
        //Serial.println("Teszt: "+csomag[sorszam]);
        //a következő csomgarészhez
        sorszam++;
        honnan=elvalaszto+1;
        elvalaszto= feldolgozando.indexOf('#',honnan);
      }
      //ha nincs már elválasztó, akkor ez az utolsó csomagdarabka
      csomag[sorszam]=feldolgozando.substring(honnan, feldolgozando.length());
      //Serial.println("Teszt: "+csomag[sorszam]);
      //levesszük a most feldolgozott bejovő adatot az elejéről
      bejovoAdat = bejovoAdat.substring(vanLezaro+1,bejovoAdat.length());
      //kivesszük a parancsot és annak megfelelően cselekszünk
      String mit;
      switch((csomag[0]).toInt()){
        case 0:
          mit=csomag[1];
          if (mit=="on")
            Serial.println("onOK");
          else if (mit=="off")
            Serial.println("offOK");
          else
            Serial.println("ERROR");
        break;
        case 1:
        break;
        default:
          Serial.println("ERROR");
        break;        
      }     
    } else {
      //ha van lezáró jel, de kezdő nincs, akkor a csomag eleje elveszett      
      //töröljük ki az első lezárójelig a bejövő adatot
      bejovoAdat=bejovoAdat.substring(vanLezaro+1,bejovoAdat.length());    
    }
    //az új csomaghoz
    vanKezdo = bejovoAdat.indexOf('>');
    vanLezaro = bejovoAdat.indexOf('|');
  }
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
void UltraServ(){
  unsigned int tav;
  
  int irany=1;  
  for(szog=kezdo;szog<=veg;szog++){
    srv.write(szog);
    delay(2);
    tav = sonar.ping_median();
    delay(145); //4*29    

    //a telefonon pont fordítva van a megjelenítés, ezért a szöget tükröznünk kell a skálán
    //és ezért az irány is fordított -irany
    //int szog2=szog;
    int szog2=180-szog;
    
    //küldés BT-on    
    Serial.println(">3#"+(String)(szog2)+"#"+(String)(tav/US_ROUNDTRIP_CM)+"#"+(-irany)+"|");
  }
  irany=-irany;
  for(szog=veg;szog>=kezdo;szog--){
    srv.write(szog);
    delay(2);
    tav = sonar.ping_median();
    delay(145); //4*29

    //a telefonon pont fordítva van a megjelenítés, ezért a szöget tükröznünk kell a skálán
    //int szog2=szog;
    int szog2=180-szog;
    
    //küldés BT-on
    Serial.println(">3#"+(String)(szog2)+"#"+(String)(tav/US_ROUNDTRIP_CM)+"#"+(-irany)+"|");
  }
}

void loop() {
  BT_beerkezo();
  UltraServ();
}
