#include <iostream.h>
#include <string.h>

#define RD(p,a,b,c,d) (*((p) + (a)*(2*3*3) + (b)*(3*3) + (c)*(3) + (d)))

#define MXA 20
#define MXD 18

#define FD(p,a,b) (*((p) + (a)*MXD + (b)))

void round(double* data);
inline void sort(int* data, int n);

double fight(double* data, double* fd , int a, int d);

int main(void)
{
 double *round_data=new double[
  3*  //Attacking with 1, 2 or 3 armies
  2*  //Defending with 1 or 2 armies
  3*  //Attacker loosing 0, 1 or 2 armies
  3   //Defender loosing 0, 1 or 2 armies
 ];
 
 double *fight_data=new double[
  MXA*
  MXD
 ];
 
 round(round_data);
  
 for (int a=0;a<3;a++)
 {
  for (int b=0;b<2;b++)
  {
   cout
    << "Attacking with " << (a+1) << " arm" << (((a+1)!=1)?("ies"):("y  "))
    << "\t\tDefending with " << (b+1) << " arm" << (((b+1)!=1)?("ies"):("y  ")) << "\n";
   for (int c=0;c<3;c++)
   {
    for (int d=0;d<3;d++)
    {
     if ( RD(round_data,a,b,c,d)!=0 )
     {
      cout
       << "\tAttacker loosing " << c << " arm" << ((c!=1)?("ies"):("y  "))
       << "\tDefender loosing " << d << " arm" << ((d!=1)?("ies"):("y  "))
       << ":\t" << ((int)(RD(round_data,a,b,c,d)*10000))/100.0 << "%\n";
     }
    }
   }
   cout << "\n";
  }
 }
 
 cout << "\n\n";
 
 for (int b=1;b<=9;b++)
  cout << "\t"<< b; 
 cout << "\n";
 for (int a=2;a<=20;a++)
 {
  cout << a << "\t";
  for (int b=1;b<=9;b++)
  {
   cout << (((int)(fight(round_data, fight_data, a, b)*10000))/100.0) << "%\t";
  }
  cout << "\n";
 }
 
 cout << "\n\n";

 for (int b=10;b<=18;b++)
  cout << "\t"<< b; 
 cout << "\n";
 for (int a=2;a<=20;a++)
 {
  cout << a << "\t";
  for (int b=10;b<=18;b++)
  {
   cout << (((int)(fight(round_data, fight_data, a, b)*10000))/100.0) << "%\t";
  }
  cout << "\n";
 }
 cout << "\n";
 
 delete [] round_data;
 delete [] fight_data;
 
 return 0;
}

double fight(double* data, double* fd, int a, int d)
{
 double prob=0;
 int ad=((a>3)?(3):(a-1)), dd=((d>2)?(2):(1));

 for (int i=0;i<3;i++)
 {
  for (int j=0;j<3;j++)
  {
   if ( RD(data, ad-1, dd-1, i, j)!=0 )
   {
    int dd_=d-j;
    
    if ( dd_==0 )
    {
     prob+=RD(data, ad-1, dd-1, i, j);
    } else
    {
     int ad_=a-i;
     if ( ad_>1 )
     {
      prob+=FD(fd, ad_, dd_)*RD(data, ad-1, dd-1, i, j);
     } 
    }
   }
  }
 }
 
 FD(fd, a, d)=prob;
  
 return prob;
}

void round(double* data)
{
 int lk[]={6, 6*6, 6*6*6};
 
 
 for (int a=0;a<3;a++)
 {
  int ad=a+1, *adie=new int[ad], *adie_=new int[ad];
  for (int b=0;b<2;b++)
  {
   int dd=b+1, *ddie=new int[dd], *ddie_=new int[dd];
   double prob=1.0/(lk[a]*lk[b]);
   int battles=((ad>dd)?(dd):(ad));
   
   for (int i=0;i<ad;i++)
    *(adie+i)=1;

    
   while ( *(adie+a)!=7 )
   {
   
    for (int i=0;i<dd;i++)
     *(ddie+i)=1;

    while ( *(ddie+b)!=7 )
    {
     
     {
      //Evaluate losses when 'ad' die are attacking 'dd' die..
      //adie contains the attacker's dice values
      //ddie contains the defender's dice values
      memcpy(adie_, adie, ad*sizeof(int));
      memcpy(ddie_, ddie, dd*sizeof(int));
      
      sort(adie_,ad);
      sort(ddie_,dd);
      
      int al=0, dl=0;
      
      for (int i=0;i<battles;i++)
       if ( (*(adie_+i)) > (*(ddie_+i)) ) dl++; else al++;
      
      RD(data, a,b, al,dl)+=prob;
     }
     

     (*ddie)++;
     for (int i=0;i<b;i++)
     {
      if ( (*(ddie+i))==7 )
      {
       *(ddie+i)=1;
       (*(ddie+i+1))++;
      }
     }
    
    }

    (*adie)++;
    for (int i=0;i<a;i++)
    {
     if ( (*(adie+i))==7 )
     {
      *(adie+i)=1;
      (*(adie+i+1))++;
     }
    }
   
   }
   delete [] ddie;
   delete [] ddie_;
  }
  delete [] adie;
  delete [] adie_;
 }
 
}

void sort(int* data, int n)
{
 for (int a=0;a<(n-1);a++)
 {
  for (int b=(a+1);b<n;b++)
  {
   if ( (*(data+a)) < (*(data+b)) )
   {
    int temp=*(data+a);
    *(data+a)=(*(data+b));
    *(data+b)=temp;
   }
  }
 }
}
