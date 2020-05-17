import React,{Component} from 'react'


class Manufacturers extends Component{

    constructor() {
        super();
        this.state={
            manufacturers: [],
        };
    }

    componentDidMount() {
        var url = "http://localhost:8080/manufacturersjson"


            fetch(url, {
                mode: 'cors',
                headers:{
                    'Accept': 'application/json',
                    'Content-Type': 'application/json',
                    'Access-Control-Allow-Origin':'http://localhost:3000',
                },
                method: 'GET',
            })
                .then(results => {
                    return results.json();
                }).then(data => {
                let manufacturers = data.map((manuf) => {
                    return (
                        <div key={manuf.id_manufacturer}>
                            <div className="name">{manuf.name_manufacturer}</div>
                        </div>
                    )
                })
                this.setState({manufacturers: manufacturers})
            })
        }

    render() {
      return (
          <div className="manufacturers">
              {this.state.manufacturers}
          </div>
      )
    }
}

export default Manufacturers;